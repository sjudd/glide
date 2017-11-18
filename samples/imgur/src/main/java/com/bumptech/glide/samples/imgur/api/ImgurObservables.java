package com.bumptech.glide.samples.imgur.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import rx.Observable;
import rx.functions.Func1;

/**
 * Observables for retrieving metadata from Imgur's API.
 */
final class ImgurObservables {

  private final ImgurService imgurService;

  ImgurObservables(ImgurService imgurService) {
    this.imgurService = imgurService;
  }

  Observable<List<Image>> getHotViralImages(@SuppressWarnings("SameParameterValue") int maxPages) {
    return Observable.range(0, maxPages)
        .flatMap(
            integer ->
                imgurService.getHotViral(integer)
                    .map(new GetData())
                    .flatMap(
                        images -> {
                          for (Iterator<Image> iterator = images.iterator(); iterator.hasNext();) {
                            if (iterator.next().is_album) {
                              iterator.remove();
                            }
                          }
                          return Observable.just(images);
                        }))
        .takeWhile(images -> !images.isEmpty())
        .scan(
            (images, images2) -> {
              List<Image> result = new ArrayList<>(images.size() + images2.size());
              result.addAll(images);
              result.addAll(images2);
              return result;
            })
        .cache();
  }

  private static class GetData implements Func1<Gallery, List<Image>> {
    @Override
    public List<Image> call(Gallery gallery) {
      return gallery.data;
    }
  }
}
