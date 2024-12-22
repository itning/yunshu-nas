import {inject, NgModule, provideAppInitializer} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {NZ_I18N, zh_CN} from 'ng-zorro-antd/i18n';
import {registerLocaleData} from '@angular/common';
import zh from '@angular/common/locales/zh';
import {FormsModule} from '@angular/forms';
import {provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {SharedModule} from "./module/shared/shared.module";
import {MusicModule} from "./module/music/music.module";
import {IndexModule} from "./module/index/index.module";
import {ThemeService} from "./theme.service";
import {httpInterceptorProviders} from "./http";
import * as dayjs from "dayjs";
import {VideoModule} from "./module/video/video.module";

registerLocaleData(zh);
dayjs.locale('zh-cn')

export const AppInitializerProvider = provideAppInitializer(() => {
  const initializerFn = ((themeService: ThemeService) => () => {
    return themeService.loadTheme();
  })(inject(ThemeService));
  return initializerFn();
});

@NgModule({
  declarations: [
    AppComponent
  ],
  bootstrap: [AppComponent],
  imports: [VideoModule,
    MusicModule,
    IndexModule,
    SharedModule,
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    BrowserAnimationsModule],
  providers: [AppInitializerProvider, {
    provide: NZ_I18N,
    useValue: zh_CN
  }, httpInterceptorProviders, provideHttpClient(withInterceptorsFromDi())]
})
export class AppModule {
}
