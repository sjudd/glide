package com.bumptech.glide.tests;

import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import java.util.HashSet;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowView;

/**
 * Adds functionality around {@link android.view.View.OnAttachStateChangeListener}s to
 * {@link ShadowView}.
 */
@Implements(View.class)
public class AttachStateShadowView extends ShadowView {
  @RealObject protected View view;
  public final Set<OnAttachStateChangeListener> attachStateListeners = new HashSet<>();
  private boolean isAttachedToWindow;

  @Implementation
  public void addOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
    attachStateListeners.add(listener);
  }

  @Implementation
  public void removeOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
    attachStateListeners.remove(listener);
  }

  @Implementation
  public void onAttachedToWindow() {
    isAttachedToWindow = true;
    for (OnAttachStateChangeListener listener : attachStateListeners) {
      listener.onViewAttachedToWindow(view);
    }
  }

  @Implementation
  public void onDetachedFromWindow() {
    isAttachedToWindow = false;
    for (OnAttachStateChangeListener listener : attachStateListeners) {
      listener.onViewDetachedFromWindow(view);
    }
  }

  @Implementation
  public boolean isAttachedToWindow() {
    return isAttachedToWindow;
  }
}
