/*
 * This file is part of Spoutcraft Launcher.
 *
 * Copyright (c) 2011 Spout LLC <http://www.spout.org/>
 * Spoutcraft Launcher is licensed under the Spout License Version 1.
 *
 * Spoutcraft Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the Spout License Version 1.
 *
 * Spoutcraft Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the Spout License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://spout.in/licensev1> for the full license,
 * including the MIT license.
 */
package com.nekocraft.launcher.rest;

import com.nekocraft.launcher.Channel;
import com.nekocraft.launcher.exceptions.RestfulAPIException;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class NekocraftBuild implements Comparable<NekocraftBuild> {
    private final String buildNumber;
    private final String minecraftVersion;

    @JsonCreator
    public NekocraftBuild(@JsonProperty("build_number") String buildNumber, @JsonProperty("build_version") String minecraftVersion) {
        this.buildNumber = buildNumber;
        this.minecraftVersion = minecraftVersion;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    @Override
    public int hashCode() {
        return buildNumber.hashCode() + minecraftVersion.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NekocraftBuild)) {
            return false;
        }
        NekocraftBuild other = (NekocraftBuild) obj;
        return other.buildNumber.equals(buildNumber) && other.minecraftVersion.equals(minecraftVersion);
    }

    @Override
    public String toString() {
        return "Build: " + buildNumber + " (MC: " + minecraftVersion + ")";
    }

    public int compareTo(NekocraftBuild o) {
        return o.buildNumber.compareTo(buildNumber);
    }

    public static synchronized List<NekocraftBuild> getBuildList() throws RestfulAPIException {
        InputStream stream = null;
        HashSet<NekocraftBuild> uniqueBuilds = new HashSet<NekocraftBuild>();
        for (Channel c : Channel.values()) {
            if (c != Channel.CUSTOM) {
                try {
                    stream = RestAPI.getCachingInputStream(new URL(RestAPI.getBuildListURL(c)), true);
                    ObjectMapper mapper = new ObjectMapper();
                    uniqueBuilds.addAll(Arrays.asList(mapper.readValue(stream, NekocraftBuild[].class)));
                } catch (IOException e) {
                    throw new RestfulAPIException("Error reading spoutcraft build list", e);
                } finally {
                    IOUtils.closeQuietly(stream);
                }
            }
        }
        ArrayList<NekocraftBuild> buildList = new ArrayList<NekocraftBuild>(uniqueBuilds);
        Collections.sort(buildList);
        return buildList;
    }
}
