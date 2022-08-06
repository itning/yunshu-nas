import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SharedModule} from "../shared/shared.module";
import {ListComponent} from './component/list/list.component';
import { EditComponent } from './component/edit/edit.component';
import { AddComponent } from './component/add/add.component';

@NgModule({
  declarations: [
    ListComponent,
    EditComponent,
    AddComponent
  ],
  imports: [
    CommonModule,
    SharedModule
  ]
})
export class MusicModule {
}
