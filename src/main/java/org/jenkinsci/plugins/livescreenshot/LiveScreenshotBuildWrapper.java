/**
 * Copyright 2013 Dr. Stefan Schimanski <sts@1stein.org>
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

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildWrapperDescriptor;
import jenkins.tasks.SimpleBuildWrapper;
import org.kohsuke.stapler.DataBoundConstructor;

public class LiveScreenshotBuildWrapper extends SimpleBuildWrapper {
	private final String fullscreenFilename;
	private final String thumbnailFilename;
	
	public String getFullscreenFilename() {
		return fullscreenFilename;
	}

	public String getThumbnailFilename() {
		return thumbnailFilename;
	}
	
	@DataBoundConstructor
	public LiveScreenshotBuildWrapper(String fullscreenFilename, String thumbnailFilename) {
		this.fullscreenFilename = fullscreenFilename;
		this.thumbnailFilename = thumbnailFilename;
	}

	@Override
	public void setUp(SimpleBuildWrapper.Context context, Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener, EnvVars initialEnvironment) {
		final LiveScreenshotAction action = new LiveScreenshotAction(build, workspace, this.fullscreenFilename, this.thumbnailFilename);
		build.addAction(action);
		context.setDisposer(new LiveScreenshotDisposer(fullscreenFilename, thumbnailFilename));
	}

	@Extension
	public static final class DescriptorImpl extends BuildWrapperDescriptor {
		@Override
		public String getDisplayName() {
			return "Show screenshot during build";
		}

		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}
	}
}
