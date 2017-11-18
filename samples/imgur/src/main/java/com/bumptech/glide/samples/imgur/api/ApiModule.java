package com.bumptech.glide.samples.imgur.api;

import dagger.Module;
import dagger.Provides;
import java.util.List;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Provides classes related to the Imgur API via Dagger.
 */
@Singleton
@Module
public final class ApiModule {

  @Singleton
  @Named("hotViralImages")
  @Provides
  Observable<List<Image>> provideHotViralImages(ImgurObservables imgurObservables) {
    return imgurObservables.getHotViralImages(5 /*maxPages*/);
  }

  @Provides ImgurObservables imgurObservables(ImgurService imgurService) {
    return new ImgurObservables(imgurService);
  }

  @Provides ImgurService getImgurService(Retrofit retrofit) {
    return retrofit.create(ImgurService.class);
  }

  @Provides Retrofit retrofit() {
    OkHttpClient client = new OkHttpClient.Builder()
        .addInterceptor(chain ->
            chain.proceed(
                chain.request()
                    .newBuilder()
                    .addHeader("Authorization", "Client-ID " + ImgurService.CLIENT_ID)
                    .build()))
        .build();
    return new Retrofit.Builder()
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .baseUrl("https://api.imgur.com/3/")
        .build();
  }
}
