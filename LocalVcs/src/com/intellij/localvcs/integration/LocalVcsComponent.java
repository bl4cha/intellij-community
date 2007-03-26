package com.intellij.localvcs.integration;

import com.intellij.ide.startup.StartupManagerEx;
import com.intellij.localvcs.ILocalVcs;
import com.intellij.localvcs.LocalVcs;
import com.intellij.localvcs.Storage;
import com.intellij.localvcs.ThreadSafeLocalVcs;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.ex.VirtualFileManagerEx;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

// todo get rid of all singletons
public class LocalVcsComponent implements ProjectComponent, ILocalVcsComponent {
  private Project myProject;
  private StartupManagerEx myStartupManager;
  private ProjectRootManagerEx myRootManager;
  private VirtualFileManagerEx myFileManager;
  private CommandProcessor myCommandProcessor;
  private Storage myStorage;
  private ILocalVcs myVcs;
  private LocalVcsService myService;

  // todo bad method - extend interface instead
  public static ILocalVcs getLocalVcsFor(Project p) {
    return ((LocalVcsComponent)getInstance(p)).getLocalVcs();
  }

  // todo try to get rid of this method (and use startActionFor(Project) instead
  public static ILocalVcsComponent getInstance(Project p) {
    return p.getComponent(ILocalVcsComponent.class);
  }

  public LocalVcsComponent(Project p, StartupManager sm, ProjectRootManagerEx rm, VirtualFileManagerEx fm, CommandProcessor cp) {
    myProject = p;
    myStartupManager = (StartupManagerEx)sm;
    myRootManager = rm;
    myFileManager = fm;
    myCommandProcessor = cp;
  }

  public void initComponent() {
    if (isDefaultProject()) return;

    if (!isEnabled()) return;

    // todo review startup order
    myStartupManager.registerPreStartupActivity(new Runnable() {
      public void run() {
        initVcs();
        initService();
      }
    });
  }

  protected void initVcs() {
    myStorage = new Storage(getStorageDir());
    myVcs = new ThreadSafeLocalVcs(new LocalVcs(myStorage));
  }

  protected void initService() {
    myService = new LocalVcsService(myVcs, new IdeaGateway(myProject), myStartupManager, myRootManager, myFileManager, myCommandProcessor);
  }

  public File getStorageDir() {
    // todo dont forget to change folder name
    File vcsDir = new File(getSystemPath(), "vcs_new");
    return new File(vcsDir, myProject.getLocationHash());
  }

  protected String getSystemPath() {
    return PathManager.getSystemPath();
  }

  public void save() {
    if (isDefaultProject()) return;

    if (!isEnabled()) return;
    if (myVcs != null) myVcs.save();
  }

  public void disposeComponent() {
    if (isDefaultProject()) return;

    if (!isEnabled()) return;
    closeVcs();
    closeService();

    cleanupStorageAfterTestCase();
  }

  protected void cleanupStorageAfterTestCase() {
    if (isUnitTestMode()) FileUtil.delete(getStorageDir());
  }

  protected void closeVcs() {
    myStorage.close();
  }

  protected void closeService() {
    myService.shutdown();
  }

  protected boolean isDefaultProject() {
    return myProject.isDefault();
  }

  public boolean isEnabled() {
    if (ApplicationManager.getApplication().isUnitTestMode()) return true;
    return System.getProperty("newlocalvcs.enabled") != null;
  }

  protected boolean isUnitTestMode() {
    return ApplicationManagerEx.getApplicationEx().isUnitTestMode();
  }

  public LocalVcsAction startAction(String label) {
    if (!isEnabled()) return LocalVcsAction.NULL;
    return myService.startAction(label);
  }

  @NonNls
  @NotNull
  public String getComponentName() {
    // todo dont forget to change name
    return "NewLocalVcs";
  }

  public ILocalVcs getLocalVcs() {
    if (!isEnabled()) throw new RuntimeException("new local vcs is disabled");
    return myVcs;
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }
}
