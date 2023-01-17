package it.lutechcdm.thingworxextensionplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFileManager;
import it.lutechcdm.thingworxextensionplugin.listeners.ThingworxVirtualFileListener;
import org.jetbrains.annotations.NotNull;

public class ThingworxStartupActivity implements StartupActivity.DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        //setup listener
        project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new ThingworxVirtualFileListener(project));
    }
}
