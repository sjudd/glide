package com.bumptech.glide.annotation.compiler;

import static com.bumptech.glide.annotation.compiler.test.Util.annotation;
import static com.bumptech.glide.annotation.compiler.test.Util.appResource;
import static com.bumptech.glide.annotation.compiler.test.Util.emptyAppModule;
import static com.bumptech.glide.annotation.compiler.test.Util.emptyLibraryModule;
import static com.bumptech.glide.annotation.compiler.test.Util.glide;
import static com.bumptech.glide.annotation.compiler.test.Util.libraryResource;
import static com.bumptech.glide.annotation.compiler.test.Util.subpackage;
import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import com.bumptech.glide.annotation.compiler.test.Util;
import com.google.common.truth.Truth;
import com.google.testing.compile.Compilation;
import java.io.IOException;
import javax.tools.JavaFileObject;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests adding both an empty {@link com.bumptech.glide.module.AppGlideModule} and an empty
 * {@link com.bumptech.glide.module.LibraryGlideModule} in a single project.
 */
public class EmptyAppAndLibraryGlideModulesTest {
  private static final String DIR_NAME = EmptyAppAndLibraryGlideModulesTest.class.getSimpleName();
  private Compilation compilation;

  @Before
  public void setUp() {
    compilation =
        javac()
            .withProcessors(new GlideAnnotationProcessor())
            .compile(
                emptyAppModule(),
                emptyLibraryModule());
    assertThat(compilation).succeededWithoutWarnings();
  }

  @Test
  public void compilation_generatesAllExpectedFiles() {
    Truth.assertThat(compilation.generatedSourceFiles()).hasSize(7);
  }

  @Test
  public void compilation_generatesExpectedGlideOptionsClass() throws IOException {
    assertThat(compilation)
        .generatedSourceFile(subpackage("GlideOptions"))
        .contentsAsUtf8String()
        .isEqualTo(appResource("GlideOptions.java").getCharContent(true));
  }

  @Test
  public void compilation_generatesExpectedGlideRequestClass() throws IOException {
    assertThat(compilation)
        .generatedSourceFile(subpackage("GlideRequest"))
        .contentsAsUtf8String()
        .isEqualTo(appResource("GlideRequest.java").getCharContent(true));
  }

  @Test
  public void compilation_generatesExpectedGlideRequestsClass() throws IOException {
    assertThat(compilation)
        .generatedSourceFile(subpackage("GlideRequests"))
        .contentsAsUtf8String()
        .isEqualTo(appResource("GlideRequests.java").getCharContent(true));
  }

  @Test
  public void compilationGeneratesExpectedGlideAppClass() throws IOException {
    assertThat(compilation)
        .generatedSourceFile(subpackage("GlideApp"))
        .contentsAsUtf8String()
        .isEqualTo(appResource("GlideApp.java").getCharContent(true));
  }

  @Test
  public void compilation_generatesExpectedGeneratedAppGlideModuleImpl() throws IOException {
    assertThat(compilation)
        .generatedSourceFile(glide("GeneratedAppGlideModuleImpl"))
        .contentsAsUtf8String()
        .isEqualTo(forResource("GeneratedAppGlideModuleImpl.java").getCharContent(true));
  }

  @Test
  public void compilation_generatesExpectedGeneratedRequestManagerFactory() throws IOException {
    assertThat(compilation)
        .generatedSourceFile(glide("GeneratedRequestManagerFactory"))
        .contentsAsUtf8String()
        .isEqualTo(appResource("GeneratedRequestManagerFactory.java").getCharContent(true));
  }

  @Test
  public void compilation_generatesExpectedIndexer() throws IOException {
    String expectedClassName =
        "GlideIndexer_GlideModule_com_bumptech_glide_test_EmptyLibraryModule";
    assertThat(compilation)
        .generatedSourceFile(annotation(expectedClassName))
        .contentsAsUtf8String()
        .isEqualTo(libraryResource(expectedClassName + ".java").getCharContent(true));
  }

  private static JavaFileObject forResource(String name) {
    return Util.forResource(DIR_NAME, name);
  }
}
