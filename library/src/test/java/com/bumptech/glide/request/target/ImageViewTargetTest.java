package com.bumptech.glide.request.target;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.tests.AttachStateShadowView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 19, shadows = AttachStateShadowView.class)
public class ImageViewTargetTest {

  @Mock private AnimatedDrawable animatedDrawable;
  private ImageView view;
  private TestTarget target;
  private ColorDrawable drawable;
  private AttachStateShadowView shadowView;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    view = new ImageView(RuntimeEnvironment.application);
    target = new TestTarget(view);
    drawable = new ColorDrawable(Color.RED);
    shadowView = Shadow.extract(view);
  }

  @Test
  public void testReturnsCurrentDrawable() {
    view.setImageDrawable(drawable);

    assertEquals(drawable, target.getCurrentDrawable());
  }

  @Test
  public void testSetsDrawableSetsDrawableOnView() {
    target.setDrawable(drawable);

    assertEquals(drawable, view.getDrawable());
  }

  @Test
  public void testSetsDrawableOnLoadStarted() {
    target.onLoadStarted(drawable);

    assertEquals(drawable, view.getDrawable());
  }

  @Test
  public void testSetDrawableOnLoadFailed() {
    target.onLoadFailed(drawable);

    assertEquals(drawable, view.getDrawable());
  }

  @Test
  public void testSetsDrawableOnLoadCleared() {
    target.onLoadCleared(drawable);

    assertEquals(drawable, view.getDrawable());
  }

  @Test
  public void testSetsDrawableOnViewInOnResourceReadyWhenAnimationReturnsFalse() {
    @SuppressWarnings("unchecked") Transition<Drawable> animation = mock(Transition.class);
    when(animation.transition(any(Drawable.class), eq(target))).thenReturn(false);
    Drawable resource = new ColorDrawable(Color.GRAY);
    target.onResourceReady(resource, animation);

    assertEquals(resource, target.resource);
  }

  @Test
  public void testDoesNotSetDrawableOnViewInOnResourceReadyWhenAnimationReturnsTrue() {
    Drawable resource = new ColorDrawable(Color.RED);
    @SuppressWarnings("unchecked") Transition<Drawable> animation = mock(Transition.class);
    when(animation.transition(eq(resource), eq(target))).thenReturn(true);
    target.onResourceReady(resource, animation);

    assertNull(target.resource);
  }

  @Test
  public void testProvidesCurrentPlaceholderToAnimationIfPresent() {
    Drawable placeholder = new ColorDrawable(Color.BLACK);
    view.setImageDrawable(placeholder);

    @SuppressWarnings("unchecked") Transition<Drawable> animation = mock(Transition.class);

    target.onResourceReady(new ColorDrawable(Color.GREEN), animation);

    verify(animation).transition(eq(placeholder), eq(target));
  }

  @Test
  public void onResourceReady_withAnimatableResource_startsAnimatableAfterSetResource() {
    AnimatedDrawable drawable = mock(AnimatedDrawable.class);
    ImageView view = mock(ImageView.class);
    target = new TestTarget(view);
    target.onStart();
    when(view.isAttachedToWindow()).thenReturn(true);
    target.onResourceReady(drawable, /*transition=*/ null);

    InOrder order = inOrder(view, drawable);
    order.verify(view).setImageDrawable(drawable);
    order.verify(drawable).start();
  }

  @Test
  public void onLoadCleared_withAnimatableDrawable_stopsDrawable() {
    target.onStart();
    shadowView.callOnAttachedToWindow();
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    verify(animatedDrawable).start();
    verify(animatedDrawable, never()).stop();

    target.onLoadCleared(/*placeholder=*/ null);

    verify(animatedDrawable).stop();
  }

  @Test
  public void onViewDetachedFromWindow_withNonAnimatableResource_doesNothing() {
    target.onResourceReady(drawable, /*transition=*/ null);
    shadowView.callOnDetachedFromWindow();
  }

  @Test
  public void onViewDetachedFromWindow_withAnimatableResource_stopsResource() {
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    shadowView.callOnDetachedFromWindow();
    verify(animatedDrawable).stop();
  }

  @Test
  public void onViewAttachedToWindow_withAnimatableResource_startsResource() {
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    target.onStart();
    reset(animatedDrawable);
    shadowView.callOnAttachedToWindow();
    verify(animatedDrawable).start();
  }

  @Test
  public void onViewAttachedToWindow_afterOnStop_doesNotStartResource() {
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    reset(animatedDrawable);
    target.onStop();
    shadowView.callOnAttachedToWindow();
    verify(animatedDrawable, never()).start();
  }

  @Test
  public void onStart_withViewAttachedToWindow_startsResource() {
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    reset(animatedDrawable);
    shadowView.callOnAttachedToWindow();
    verify(animatedDrawable, never()).start();
    target.onStart();
    verify(animatedDrawable).start();
  }

  @Test
  public void onStart_withViewDetachedFromWindow_doesNotStartResource() {
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    reset(animatedDrawable);
    shadowView.callOnDetachedFromWindow();
    target.onStart();
    verify(animatedDrawable, never()).start();
  }

  @Test
  public void onLoadCleared_withAnimatableResource_removesListener() {
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    target.onLoadCleared(/*placeholder=*/ null);
    assertThat(shadowView.attachStateListeners).isEmpty();
  }

  @Test
  public void onResourceReady_afterViewAttachedWithPreviousAnimatableThenDetached_doesNotStart() {
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    shadowView.callOnAttachedToWindow();
    target.onLoadCleared(/*placeholder=*/ null);
    shadowView.callOnDetachedFromWindow();

    animatedDrawable = mock(AnimatedDrawable.class);
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    verify(animatedDrawable, never()).start();
  }

  @Test
  public void onResourceReady_withStoppedAttachedView_doesNotCallStart() {
    target.onStop();
    shadowView.onAttachedToWindow();
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    verify(animatedDrawable, never()).start();
  }

  @Test
  public void onResourceReady_withStartedDetachedView_doesNotCallStart() {
    target.onStart();
    shadowView.onDetachedFromWindow();
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    verify(animatedDrawable, never()).start();
  }

  @Test
  public void onLoadStarted_withAnimatableResource_unregistersListener() {
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    target.onLoadStarted(/*placeholder=*/ null);
    assertThat(shadowView.attachStateListeners).isEmpty();
  }

  @Test
  public void onLoadStarted_withAnimatableResource_stopsResource() {
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    reset(animatedDrawable);
    target.onLoadStarted(/*placeholder=*/ null);

    verify(animatedDrawable).stop();
  }

  @Test
  public void onLoadFailed_withAnimatableResource_unregistersListener() {
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    target.onLoadFailed(/*placeholder=*/ null);
    assertThat(shadowView.attachStateListeners).isEmpty();
  }

  @Test
  public void onLoadFailed_withAnimatableResource_stopsResource() {
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    reset(animatedDrawable);
    target.onLoadFailed(/*placeholder=*/ null);

    verify(animatedDrawable).stop();
  }

  @Test
  public void onResourceReady_multipleTimesInARow_registersOnlyOneListener() {
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    target.onResourceReady(animatedDrawable, /*transition=*/ null);
    target.onResourceReady(animatedDrawable, /*transition=*/ null);

    assertThat(shadowView.attachStateListeners).hasSize(1);

    target.onLoadCleared(/*placeholder=*/ null);

    assertThat(shadowView.attachStateListeners).isEmpty();
  }

  private abstract static class AnimatedDrawable extends Drawable implements Animatable {
    // Intentionally empty.
  }

  private static final class TestTarget extends ImageViewTarget<Drawable> {
    public Drawable resource;

    TestTarget(ImageView view) {
      super(view);
    }

    @Override
    protected void setResource(Drawable resource) {
      this.resource = resource;
      view.setImageDrawable(resource);
    }
  }
}
