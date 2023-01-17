package it.lutechcdm.thingworxextensionplugin;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import it.lutechcdm.thingworxextensionplugin.config.MetadataConfigFile;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

public class ThingworxProjectUtils {

    public static boolean isThingworxProject(Project project) {
        String projectBasePath = getThingworxProjectBasePath(project);
        if (projectBasePath == null)
            return false;

        File twxLibFolder = new File(projectBasePath, ThingworxConstants.TWX_LIB);
        if (twxLibFolder.exists()) {
            if (twxLibFolder.listFiles((dir, name) -> name != null && name.toLowerCase().matches("^thingworx-ext-sdk-.+\\.jar$")).length > 0)
                return true;
        }

        VirtualFile[] moduleContentRoots = ProjectRootManager.getInstance(project).getContentRootsFromAllModules();
        for (VirtualFile moduleContentRoot : moduleContentRoots) {
            twxLibFolder = new File(moduleContentRoot.getPath(), ThingworxConstants.TWX_LIB);
            if (twxLibFolder.listFiles((dir, name) ->  name != null && name.toLowerCase().matches("^thingworx-ext-sdk-.+\\.jar$")).length > 0)
                return true;
        }
        return false;
    }


    public static String getThingworxProjectBasePath(Project project) {
        if(new File(project.getBasePath(), ThingworxConstants.TWX_LIB).exists()) {
            return project.getBasePath();
        }

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for(Module module : modules) {
            if(ProjectUtil.guessModuleDir(module) == null)
                continue;

            if(new File(ProjectUtil.guessModuleDir(module).getPath(), ThingworxConstants.TWX_LIB).exists()) {
                return ProjectUtil.guessModuleDir(module).getPath();
            }
        }
        return null;
    }

    public static void removeJarToModuleBuildDependencies(Project project, String jarFile) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            ModifiableRootModel modifiableRootModel = ModuleRootManager.getInstance(module).getModifiableModel();

            Library library = modifiableRootModel.getModuleLibraryTable().getLibraryByName(ThingworxConstants.MODULE_LIBRARY_NAME);
            if(library != null) {
                final Library.ModifiableModel libModel = library.getModifiableModel();
                for(String url : libModel.getUrls(OrderRootType.CLASSES)) {
                    if(url.endsWith(jarFile))
                        libModel.removeRoot(url, OrderRootType.CLASSES);
                }
                libModel.commit();

                if(library.getFiles(OrderRootType.CLASSES).length == 0) {
                    for (final OrderEntry orderEntry : modifiableRootModel.getOrderEntries()) {
                        if (orderEntry instanceof LibraryOrderEntry && StringUtil.equals(library.getName(), ((LibraryOrderEntry)orderEntry).getLibraryName()))
                            modifiableRootModel.removeOrderEntry(orderEntry);
                    }
                }
            }

            if(modifiableRootModel.isChanged())
                modifiableRootModel.commit();
            break;
        }
    }

    public static void addJarToModuleBuildDependencies(Project project, VirtualFile newJarFile) {

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            ModifiableRootModel modifiableRootModel = ModuleRootManager.getInstance(module).getModifiableModel();

            Library library = modifiableRootModel.getModuleLibraryTable().getLibraryByName(ThingworxConstants.MODULE_LIBRARY_NAME);
            if(library == null) {
                library = modifiableRootModel.getModuleLibraryTable().createLibrary(ThingworxConstants.MODULE_LIBRARY_NAME);
            }
            final Library.ModifiableModel libModel = library.getModifiableModel();
            libModel.addRoot(newJarFile, OrderRootType.CLASSES);
            libModel.commit();

            if(modifiableRootModel.isChanged())
                modifiableRootModel.commit();
            break;
        }
    }

    @Nullable
    public static XmlFile getMetadataFile(Project project) {
       return getMetadataFile(project, true);
    }

    public static XmlFile getMetadataFile(Project project, boolean withRefresh) {
        String basePath = getThingworxProjectBasePath(project);
        if(basePath == null)
            return null;

        if(withRefresh)
            LocalFileSystem.getInstance().refresh(false);
        VirtualFile metadataFile = LocalFileSystem.getInstance().findFileByIoFile(Path.of(basePath, ThingworxConstants.CONFIG_FOLDER, MetadataConfigFile.METADATA_FILE_NAME).toFile());
        if(metadataFile == null)
            return null;

        return (XmlFile) PsiManager.getInstance(project).findFile(metadataFile);
    }
}
