/**
 * Copyright 2013 Dr. Stefan Schimanski <sts@1stein.org>
 * Copyright 2017-2018 Harald Sitter <sitter@kde.org>
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

import hudson.Extension;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.util.RunList;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sts
 */
public class LiveScreenshotColumn extends ListViewColumn {
	
	@DataBoundConstructor
    public LiveScreenshotColumn() {
    }
	
	public String collectScreenshots(Run run) {
		String html = "";
		
		// sub-runs of matrix?
		if (run instanceof MatrixBuild) {
			MatrixBuild mb = (MatrixBuild)run;
			List<MatrixRun> childRuns = mb.getRuns();
			StringBuffer buf = new StringBuffer();
			for (MatrixRun child : childRuns) {
				buf.append(collectScreenshots(child));
			}
			html += buf.toString();
		} else {
			// add screenshot of current job
			if (run.isBuilding()) {
				html = html + "<a href=\"/" + run.getUrl() + "screenshot\">" +
						"<img src=\"/" + run.getUrl() + "screenshot/thumb\" /></a>";
			}
		}
		return html;
	}

	public String getScreenshots(Job job) {
		// collect screenshot link strings for all active builds
		RunList runs = job.getBuilds();
		HashMap<Build, String> runScreenshotStrings = new HashMap<Build, String>();
		for (Object o : runs) {
			Build b = (Build)o;
			if (!b.isBuilding())
				continue;
			String rs = this.collectScreenshots(b);
			if (rs.isEmpty())
				continue;
			if (runScreenshotStrings.containsKey(b)) {
					runScreenshotStrings.put(b, runScreenshotStrings.get(b) + rs);
			} else {
				runScreenshotStrings.put(b, rs);
			}


		}

		// one row for each job
		StringBuffer buf = new StringBuffer();
		for (Map.Entry<Build, String> pair : runScreenshotStrings.entrySet()) {
			// newline?
			if (buf.length() != 0) {
				buf.append("<br/><br/>");
			}
			
			// first the screenshots
			buf.append(pair.getValue());

			// then the line with the "stop" link and the changelog
			Build r = pair.getKey();
			buf.append("<br/><a href=\"" + r.getUrl() + "\">" + r.getDisplayName() + "</a> ");

			// create link to executor stop action, or the oneOffExecutor for MatrixBuilds
			Executor executor = null;
			boolean isOneOffExecutor = r.getOneOffExecutor() != null;
			if (isOneOffExecutor) {
				executor = r.getOneOffExecutor();
			} else {
				executor = r.getExecutor();
			}
			if (executor != null) {
				Computer computer = executor.getOwner();
				if (computer != null) {
					buf.append("<a href=\"" + computer.getUrl() +
							(isOneOffExecutor ? "oneOffExecutors" : "executors") +
							"/" +
							(isOneOffExecutor ? computer.getOneOffExecutors().indexOf(executor) : executor.getNumber()) +
							"/stop\">Stop</a> ");
				}
			}

			// append changelog entries
			ChangeLogSet<? extends Entry> changeLogSet = r.getChangeSet();
			if (changeLogSet != null) {
				for (Object o : changeLogSet.getItems()) {
					if (o instanceof ChangeLogSet.Entry) {
						ChangeLogSet.Entry e = (ChangeLogSet.Entry) o;
						buf.append(" - " + e.getMsgAnnotated());
					}
				}
			}
		}
		
        return buf.toString();
    }
    
    @Extension
    public static class LiveScreenshotColumnDescriptor extends ListViewColumnDescriptor {

        @Override
        public String getDisplayName() {
            return "Screenshots";
        }
    }
}
