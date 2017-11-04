package com.bumptech.glide.annotation.compiler;

import static com.bumptech.glide.annotation.compiler.test.Util.glide;
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
 * Tests adding a single {@link com.bumptech.glide.test.EmptyAppModule} in a project.
 */
public class EmptyAppGlideModuleTest {
  private static final String DIR_NAME = "EmptyAppGlideModuleTest";
  private static final String MODULE_NAME = "EmptyAppModule.java";
  private Compilation compilation;

  @Before
  public void setUp() {
    compilation =
        javac()
            .withProcessors(new GlideAnnotationProcessor())
            .compile(forResource(MODULE_NAME));
    assertThat(compilation).succeededWithoutWarnings();
  }

  @Test
  public void compilation_generatesAllExpectedFiles() {
    Truth.assertThat(compilation.generatedSourceFiles()).hasSize(6);
  }

  @Test
  public void compilation_generatesExpectedGlideOptionsClass() throws IOException {
    assertThat(compilation)
        .generatedSourceFile(subpackage("GlideOptions"))
        .contentsAsUtf8String()
        .isEqualTo(forResource("GlideOptions.java").getCharContent(true));
  }

  @Test
  public void compilation_generatesExpectedGlideRequestClass() throws IOException {
    assertThat(compilation)
        .generatedSourceFile(subpackage("GlideRequest"))
        .contentsAsUtf8String()
        .isEqualTo(forResource("GlideRequest.java").getCharContent(true));
  }

  @Test
  public void compilation_generatesExpectedGlideRequestsClass() throws IOException {
    assertThat(compilation)
        .generatedSourceFile(subpackage("GlideRequests"))
        .contentsAsUtf8String()
        .isEqualTo(forResource("GlideRequests.java").getCharContent(true));
  }

  @Test
  public void compilationGeneratesExpectedGlideAppClass() throws IOException {
    assertThat(compilation)
        .generatedSourceFile(subpackage("GlideApp"))
        .contentsAsUtf8String()
        .isEqualTo(forResource("GlideApp.java").getCharContent(true));
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
        .isEqualTo(forResource("GeneratedRequestManagerFactory.java").getCharContent(true));
  }

  private static JavaFileObject forResource(String name) {
    return Util.forResource(DIR_NAME, name);
  }
}

