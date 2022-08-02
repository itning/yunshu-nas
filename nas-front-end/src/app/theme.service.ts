import {Injectable} from '@angular/core';
import {PersistenceService} from './service/persistence.service';

export enum ThemeType {
  dark = 'dark',
  default = 'default',
}

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private static readonly DEFAULT_THEME = 'DEFAULT_THEME';

  currentTheme = ThemeType.default;

  constructor(private persistenceService: PersistenceService) {
    const defaultTheme = this.persistenceService.getMetaInfo(ThemeService.DEFAULT_THEME);
    if (ThemeType.default === defaultTheme) {
      this.currentTheme = ThemeType.default;
    } else if (ThemeType.dark === defaultTheme) {
      this.currentTheme = ThemeType.dark;
    } else {
      this.currentTheme = ThemeType.default;
      this.persistenceService.saveMetaInfo(ThemeService.DEFAULT_THEME, ThemeType.default);
    }
  }

  private reverseTheme(theme: string): ThemeType {
    return theme === ThemeType.dark ? ThemeType.default : ThemeType.dark;
  }

  private removeUnusedTheme(theme: ThemeType): void {
    document.documentElement.classList.remove(theme);
    const removedThemeStyle = document.getElementById(theme);
    if (removedThemeStyle) {
      document.head.removeChild(removedThemeStyle);
    }
  }

  private loadCss(href: string, id: string): Promise<Event> {
    return new Promise((resolve, reject) => {
      const style = document.createElement('link');
      style.rel = 'stylesheet';
      style.href = href;
      style.id = id;
      style.onload = resolve;
      style.onerror = reject;
      document.head.append(style);
    });
  }

  public loadTheme(firstLoad = true): Promise<Event> {
    const theme = this.currentTheme;
    if (firstLoad) {
      document.documentElement.classList.add(theme);
    }
    return new Promise<Event>((resolve, reject) => {
      this.loadCss(`${theme}.css`, theme).then(
        (e) => {
          if (!firstLoad) {
            document.documentElement.classList.add(theme);
          }
          this.removeUnusedTheme(this.reverseTheme(theme));
          resolve(e);
        },
        (e) => reject(e)
      );
    });
  }

  public toggleTheme(): Promise<Event> {
    this.currentTheme = this.reverseTheme(this.currentTheme);
    return this.loadTheme(false).then(it => {
      this.persistenceService.saveMetaInfo(ThemeService.DEFAULT_THEME, this.currentTheme);
      return it;
    });
  }
}
