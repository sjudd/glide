package com.bumptech.glide.request.target;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.widget.ImageView;
import com.bumptech.glide.request.transition.Transition;

/**
 * A base {@link com.bumptech.glide.request.target.Target} for displaying resources in {@link
 * android.widget.ImageView}s.
 *
 * @param <Z> The type of resource that this target will display in the wrapped {@link
 *            android.widget.ImageView}.
 */
// Public API.
@SuppressWarnings("WeakerAccess")
public abstract class ImageViewTarget<Z> extends ViewTarget<ImageView, Z>
    implements Transition.ViewAdapter {

  @Nullable
  private Animatable animatable;
  @Nullable
  private OnAttachStateChangeListener changeAnimatableState;
  private boolean isStarted;
  private boolean isAttachedToWindow;

  public ImageViewTarget(ImageView view) {
    super(view);
  }

  public ImageViewTarget(ImageView view, boolean waitForLayout) {
    super(view, waitForLayout);
  }

  /**
   * Returns the current {@link android.graphics.drawable.Drawable} being displayed in the view
   * using {@link android.widget.ImageView#getDrawable()}.
   */
  @Override
  @Nullable
  public Drawable getCurrentDrawable() {
    return view.getDrawable();
  }

  /**
   * Sets the given {@link android.graphics.drawable.Drawable} on the view using {@link
   * android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)}.
   *
   * @param drawable {@inheritDoc}
   */
  @Override
  public void setDrawable(Drawable drawable) {
    view.setImageDrawable(drawable);
  }

  /**
   * Sets the given {@link android.graphics.drawable.Drawable} on the view using {@link
   * android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)}.
   *
   * @param placeholder {@inheritDoc}
   */
  @CallSuper
  @Override
  public void onLoadStarted(@Nullable Drawable placeholder) {
    super.onLoadStarted(placeholder);
    setResourceInternal(null);
    setDrawable(placeholder);
  }

  /**
   * Sets the given {@link android.graphics.drawable.Drawable} on the view using {@link
   * android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)}.
   *
   * @param errorDrawable {@inheritDoc}
   */
  @CallSuper
  @Override
  public void onLoadFailed(@Nullable Drawable errorDrawable) {
    super.onLoadFailed(errorDrawable);
    setResourceInternal(null);
    setDrawable(errorDrawable);
  }

  /**
   * Sets the given {@link android.graphics.drawable.Drawable} on the view using {@link
   * android.widget.ImageView#setImageDrawable(android.graphics.drawable.Drawable)}.
   *
   * @param placeholder {@inheritDoc}
   */
  @CallSuper
  @Override
  public void onLoadCleared(@Nullable Drawable placeholder) {
    super.onLoadCleared(placeholder);
    setResourceInternal(null);
    setDrawable(placeholder);
  }

  @CallSuper
  @Override
  public void onResourceReady(Z resource, @Nullable Transition<? super Z> transition) {
    if (transition == null || !transition.transition(resource, this)) {
      setResourceInternal(resource);
    } else {
      maybeUpdateAnimatable(resource);
    }
  }

  @CallSuper
  @Override
  public void onStart() {
    isStarted = true;
    maybeStartAnimatable();
  }

  @CallSuper
  @Override
  public void onStop() {
    isStarted = false;
    maybeStopAnimatable();
  }

  private void setResourceInternal(@Nullable Z resource) {
    // Order matters here. Set the resource first to make sure that the Drawable has a valid and
    // non-null Callback before starting it.
    setResource(resource);
    maybeUpdateAnimatable(resource);
  }

  private void maybeUpdateAnimatable(@Nullable Z resource) {
    if (resource instanceof Animatable) {
      animatable = (Animatable) resource;

      // Avoid short circuiting because doing so can break sdk checks on older versions.
      //noinspection SimplifiableIfStatement
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        isAttachedToWindow = view.isAttachedToWindow();
      } else {
        // Default to true here because it's the common case.
        isAttachedToWindow = true;
      }

      if (changeAnimatableState == null) {
        changeAnimatableState = new ChangeAnimatableState();
        view.addOnAttachStateChangeListener(changeAnimatableState);
      }
      maybeStartAnimatable();
    } else {
      maybeStopAnimatable();
      if (changeAnimatableState != null) {
        view.removeOnAttachStateChangeListener(changeAnimatableState);
        changeAnimatableState = null;
      }
      animatable = null;
    }
  }

  private void maybeStartAnimatable() {
    if (animatable != null && isStarted && isAttachedToWindow) {
      animatable.start();
    }
  }

  private void maybeStopAnimatable() {
    if (animatable != null) {
      animatable.stop();
    }
  }

  protected abstract void setResource(@Nullable Z resource);

  private final class ChangeAnimatableState implements OnAttachStateChangeListener {

    @Override
    public void onViewAttachedToWindow(View v) {
      isAttachedToWindow = true;
      maybeStartAnimatable();
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
      isAttachedToWindow = false;
      maybeStopAnimatable();
    }
  }
}

