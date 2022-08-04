import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from "../shared/shared.module";
import { ListComponent } from './component/list/list.component';
import { PlayComponent } from './component/play/play.component';

@NgModule({
  declarations: [
    ListComponent,
    PlayComponent
  ],
  imports: [
    CommonModule,
    SharedModule
  ]
})
export class VideoModule {
}
