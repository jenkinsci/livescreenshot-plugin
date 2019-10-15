/**
 * Copyright 2017 Harald Sitter <sitter@kde.org>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jenkinsci.plugins.livescreenshot;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.tasks.SimpleBuildWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LiveScreenshotDisposer extends SimpleBuildWrapper.Disposer {
    private static final long serialVersionUID = 1L;

    private final String fullscreenFilename;
    private final String thumbnailFilename;

    public LiveScreenshotDisposer(final String fullscreenFilename, final String thumbnailFilename) {
        this.fullscreenFilename = fullscreenFilename;
        this.thumbnailFilename = thumbnailFilename;
    }

    @Override
    public void tearDown(final Run<?, ?> build, final FilePath workspace, final Launcher launcher, final TaskListener listener) throws IOException, InterruptedException {
        Map<String, String> map = new HashMap<>();

        if (fullscreenFilename != null) {
            map.put("screenshots/" + fullscreenFilename, fullscreenFilename);
        }
        if (thumbnailFilename != null) {
            map.put("screenshots/" + thumbnailFilename, thumbnailFilename);
        }

        build.getArtifactManager().archive(workspace, launcher, null, map);
    }
}
