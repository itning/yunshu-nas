import {Component, OnInit} from '@angular/core';
import {ThemeService, ThemeType} from "../../../../theme.service";

@Component({
    selector: 'app-index',
    templateUrl: './index.component.html',
    styleUrls: ['./index.component.scss'],
    standalone: false
})
export class IndexComponent implements OnInit {

  isCollapsed = true;

  isDarkMode: boolean;

  constructor(private themeService: ThemeService) {
  }

  ngOnInit(): void {
    this.isDarkMode = this.themeService.currentTheme === ThemeType.dark;
  }

  toggleTheme(): void {
    this.themeService.toggleTheme().then();
  }

}
