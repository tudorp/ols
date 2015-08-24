/*
 * OpenBench LogicSniffer / SUMP project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *
 * Copyright (C) 2006-2010 Michael Poppitz, www.sump.org
 * Copyright (C) 2010 J.W. Janssen, www.lxtreme.nl
 * Copyright (C) 2015 Florian Frankenberger
 */
package nl.lxtreme.ols.device.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nl.lxtreme.ols.api.acquisition.*;
import nl.lxtreme.ols.api.data.*;
import nl.lxtreme.ols.api.devices.*;

/**
 * Denotes an acquisition task that imports data from a given csv file. The file needs
 * to have this layout:
 *
 * <pre>
 * [Channel Data A], [Channel Data B], ..., [Channel Data X]
 * </pre>
 *
 * Everything not numeric is ignored.
 */
public class CSVAcquisitionTask implements AcquisitionTask {
    private final CSVDeviceDialog configDialog;
    private final AcquisitionProgressListener progressListener;

    /**
     * Creates a new TestDevice instance.
     *
     * @param aConfigDialog
     * @param aProgressListener
     */
    public CSVAcquisitionTask(final CSVDeviceDialog aConfigDialog, final AcquisitionProgressListener aProgressListener) {
        this.configDialog = aConfigDialog;
        this.progressListener = aProgressListener;
    }

    private final class ChannelData {
        private final List<Integer> rawData = new ArrayList<Integer>();
        private boolean setupDone = false;
        private float meanValue;

        public void addRawData(int rawData) {
            this.rawData.add(rawData);
            setupDone = false;
        }

        public boolean isEmpty() {
            return this.rawData.isEmpty();
        }

        public int length() {
            return this.rawData.size();
        }

        public List<Integer> getRawData() {
            return rawData;
        }

        public void setup(int length) {
            while (rawData.size() > length) {
                rawData.remove(rawData.size() - 1);
            }
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            for (int value : rawData) {
                if (value < min) {
                    min = value;
                }
                if (value > max) {
                    max = value;
                }
            }
            meanValue = ((max - min) / 2f) + min;
            System.out.println("mean is: " + meanValue);
            setupDone = true;
        }

        public int getHighLowAt(int index) {
            if (!setupDone) {
                throw new IllegalStateException("You need to calculate the mean value first");
            }
            if (index >= this.rawData.size()) {
                return 0;
            } else {
                return rawData.get(index) > meanValue ? 1 : 0;
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AcquisitionResult call() throws Exception {
        final File file = this.configDialog.getFile();

        int channels = 0;
        int trigger = 0;

        //collect all data
        List<ChannelData> channelDatas = new ArrayList<ChannelData>();
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;
        while ((line = in.readLine()) != null) {
            String[] parts = line.split(",");
            for (int i = 0; i < parts.length; ++i) {
                String part = parts[i];
                if (channelDatas.size() <= i) {
                    channelDatas.add(new ChannelData());
                }
                ChannelData data = channelDatas.get(i);
                try {
                    Integer partInt = Integer.valueOf(part.trim());
                    data.addRawData(partInt);
                } catch (NumberFormatException e) {
                    //ignore for now
                }
            }
        }

        //remove empty lists and count channels
        int minLength = Integer.MAX_VALUE;
        for (Iterator<ChannelData> iterator = channelDatas.iterator(); iterator.hasNext();) {
            final ChannelData channelData = iterator.next();
            if (!channelData.isEmpty()) {
                if (channelData.length() < minLength) {
                    minLength = channelData.length();
                }
                channels++;
            } else {
                iterator.remove();
            }
        }

        for (ChannelData channelData : channelDatas) {
            channelData.setup(minLength);
        }

        int[] data = new int[minLength];
        for (int i = 0; i < minLength; ++i) {
            for (int j = 0; j < channelDatas.size(); ++j) {
                ChannelData channelData = channelDatas.get(j);
                data[i] = data[i] | (channelData.getHighLowAt(i) << j);
            }
        }

        final int enabledChannels = (int) ((1L << channels) - 1);
        return new CapturedData(data, trigger, data.length, channels, enabledChannels);
    }
}
