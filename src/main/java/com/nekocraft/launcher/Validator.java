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
package com.nekocraft.launcher;

import com.nekocraft.launcher.api.Launcher;
import com.nekocraft.launcher.exceptions.RestfulAPIException;
import com.nekocraft.launcher.rest.Library;
import com.nekocraft.launcher.rest.Minecraft;
import com.nekocraft.launcher.util.MD5Utils;

import java.io.File;
import java.util.List;

public class Validator {
    private boolean passed = false;
    private boolean errors = false;

    public void run(NekocraftData build) {
        Launcher.getGameUpdater().setStartValidationTime(System.currentTimeMillis());
        try {
            errors = !validate(build);
        } catch (RestfulAPIException e) {
            e.printStackTrace();
        }
        Launcher.getGameUpdater().validationFinished(passed);
    }

    /**
     * Returns true if validation completed without errors, false if something went wrong deleting files
     *
     * @return true on validation completion, false on failure
     * @throws RestfulAPIException
     */
    private boolean validate(NekocraftData build) throws RestfulAPIException {
        File minecraftJar = new File(Launcher.getGameUpdater().getBinDir(), "minecraft.jar");
        if (minecraftJar.exists()) {
            if (!compareMD5(build.getMinecraft().getMd5(), minecraftJar)) {
                //Launcher.err("Invalid minecraft.jar");
                return minecraftJar.delete();
            }
        } else {
            Launcher.err("There is no minecraft.jar!");
            return true;
        }

        File spoutcraft = new File(Launcher.getGameUpdater().getBinDir(), "spoutcraft.jar");
        if (spoutcraft.exists()) {
            if (!compareMD5(build.getMD5(), spoutcraft)) {
                Launcher.err("Invalid spoutcraft.jar");
                return spoutcraft.delete();
            }
        } else {
            Launcher.err("There is no spoutcraft.jar");
            return true;
        }

        final Minecraft minecraft = build.getMinecraft();
        final String jinputMD5 = UpdateThread.findMd5("jinput", null, minecraft.getLibraries());

        File jinputJar = new File(Launcher.getGameUpdater().getBinDir(), "jinput.jar");
        if (jinputJar.exists()) {
            if (!compareMD5(jinputMD5, jinputJar)) {
                Launcher.err("Invalid jinput.jar");
                return jinputJar.delete();
            }
        } else {
            Launcher.err("There is no jinput.jar");
            return true;
        }

        final String lwjglMD5 = UpdateThread.findMd5("lwjgl", null, minecraft.getLibraries());

        File lwjglJar = new File(Launcher.getGameUpdater().getBinDir(), "lwjgl.jar");
        if (lwjglJar.exists()) {
            if (!compareMD5(lwjglMD5, lwjglJar)) {
                Launcher.err("Invalid lwjgl.jar");
                return lwjglJar.delete();
            }
        } else {
            Launcher.err("There is no lwjgl.jar");
            return true;
        }

        final String lwjgl_utilMD5 = UpdateThread.findMd5("lwjgl_util", null, minecraft.getLibraries());

        File lwjgl_utilJar = new File(Launcher.getGameUpdater().getBinDir(), "lwjgl_util.jar");
        if (lwjgl_utilJar.exists()) {
            if (!compareMD5(lwjgl_utilMD5, lwjgl_utilJar)) {
                Launcher.err("Invalid lwjgl_util.jar");
                return lwjgl_utilJar.delete();
            }
        } else {
            Launcher.err("There is no lwjgl_util.jar");
            return true;
        }

        File libDir = new File(Launcher.getGameUpdater().getBinDir(), "lib");
        List<Library> libraries = build.getLibraries();
        for (Library lib : libraries) {
            File libraryFile = new File(libDir, lib.name() + ".jar");

            if (libraryFile.exists()) {
                String md5 = MD5Utils.getMD5(libraryFile);
                if (!lib.valid(md5)) {
                    Launcher.err("Invalid " + libraryFile.getName());
                    return libraryFile.delete();
                }
            } else {
                Launcher.err("There is no " + libraryFile.getName());
                return true;
            }
        }
        passed = true;
        return true;
    }

    /**
     * Returns true if the validator confirmed that all the files were correct
     *
     * @return passed validation
     */
    public boolean isValid() {
        return passed;
    }

    /**
     * Returns true if the validator encountered an error while validating
     *
     * @return true if an error occured
     */
    public boolean hasErrors() {
        return errors;
    }

    private boolean compareMD5(String expected, File file) {
        String actual = MD5Utils.getMD5(file);
        Launcher.debug("Checking MD5 of " + file.getName() + ". Expected MD5: " + expected + " | Actual MD5: " + actual);
        if (expected == null || actual == null) {
            return false;
        }
        return expected.equals(actual);
    }
}
