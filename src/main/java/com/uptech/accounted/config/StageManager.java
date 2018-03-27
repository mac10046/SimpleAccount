package com.uptech.accounted.config;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Objects;

import org.slf4j.Logger;

import com.uptech.accounted.view.FxmlView;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class StageManager {

  private static final Logger LOG = getLogger(StageManager.class);
  private final Stage primaryStage;
  private final SpringFXMLLoader springFXMLLoader;

  public StageManager(SpringFXMLLoader springFXMLLoader, Stage stage) {
    this.springFXMLLoader = springFXMLLoader;
    this.primaryStage = stage;
  }

  public void switchScene(final FxmlView view) {
    Parent viewRootNodeHierarchy = loadViewNodeHierarchy(view.getFxmlFile());
    show(viewRootNodeHierarchy, view.getTitle());
  }

  private void show(final Parent rootnode, String title) {
    Scene scene = prepareScene(rootnode);
    primaryStage.setTitle(title);
    primaryStage.setScene(scene);
    if (!title.equalsIgnoreCase("login"))
      primaryStage.setMaximized(true);
    primaryStage.centerOnScreen();
    ImageView imageView = new ImageView();
    Image imgLogo = new Image(getClass().getResourceAsStream("/images/a-logo.png"));
    imageView.setImage(imgLogo);
    imageView.setPreserveRatio(false);
    imageView.setSmooth(true);
    imageView.setCache(true);
    primaryStage.getIcons().add(imgLogo);
    try {
      primaryStage.show();
    } catch (Exception exception) {
      logAndExit("Unable to show scene for title" + title, exception);
    }
  }

  private Scene prepareScene(Parent rootnode) {
    Scene scene = primaryStage.getScene();

    if (scene == null) {
      scene = new Scene(rootnode);
    }
    scene.setRoot(rootnode);
    return scene;
  }

  /**
   * Loads the object hierarchy from a FXML document and returns to root node of
   * that hierarchy.
   *
   * @return Parent root node of the FXML document hierarchy
   */
  private Parent loadViewNodeHierarchy(String fxmlFilePath) {
    Parent rootNode = null;
    try {
      rootNode = springFXMLLoader.load(fxmlFilePath);
      Objects.requireNonNull(rootNode, "A Root FXML node must not be null");
    } catch (Exception exception) {
      logAndExit("Unable to load FXML view" + fxmlFilePath, exception);
    }
    return rootNode;
  }

  private void logAndExit(String errorMsg, Exception exception) {
    LOG.error(errorMsg, exception, exception.getCause());
    Platform.exit();
  }

}
