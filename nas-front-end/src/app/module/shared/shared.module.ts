import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NzButtonModule} from 'ng-zorro-antd/button';
import {NzInputModule} from 'ng-zorro-antd/input';
import {NzFormModule} from 'ng-zorro-antd/form';
import {DateReadablePipe} from '../../pipe/date-readable.pipe';
import {NzImageModule} from "ng-zorro-antd/image";
import {AppRoutingModule} from "../../app-routing.module";
import {MusicTypeReadablePipe} from '../../pipe/music-type-readable.pipe';
import {NzLayoutModule} from 'ng-zorro-antd/layout';
import {NzMenuModule} from 'ng-zorro-antd/menu';
import {NzSwitchModule} from 'ng-zorro-antd/switch';
import {NzPopconfirmModule} from 'ng-zorro-antd/popconfirm';
import {NzTableModule} from 'ng-zorro-antd/table';
import {NzToolTipModule} from 'ng-zorro-antd/tooltip';
import {NzMessageModule} from 'ng-zorro-antd/message';
import {NzIconModule} from 'ng-zorro-antd/icon';
import {NzBreadCrumbModule} from 'ng-zorro-antd/breadcrumb';
import {NzBackTopModule} from 'ng-zorro-antd/back-top';
import {NzCardModule} from 'ng-zorro-antd/card';
import {NzSelectModule} from 'ng-zorro-antd/select';
import {NzCheckboxModule} from 'ng-zorro-antd/checkbox';
import {NzDividerModule} from 'ng-zorro-antd/divider';
import {NzInputNumberModule} from 'ng-zorro-antd/input-number';
import {Base64DecoderPipe} from "../../pipe/base64-decoder.pipe";

import {
  CodeOutline,
  CustomerServiceOutline,
  FolderOutline,
  MenuFoldOutline,
  MenuUnfoldOutline,
  MinusCircleOutline,
  PlusOutline,
  VideoCameraOutline,
} from '@ant-design/icons-angular/icons';

const icons = [
  VideoCameraOutline,
  CustomerServiceOutline,
  MenuUnfoldOutline,
  MenuFoldOutline,
  FolderOutline,
  CodeOutline,
  PlusOutline,
  MinusCircleOutline,
];

const modules = [
  CommonModule,
  ReactiveFormsModule,
  FormsModule,
  NzIconModule,
  AppRoutingModule,
  NzButtonModule,
  NzInputModule,
  NzFormModule,
  NzImageModule,
  NzLayoutModule,
  NzMenuModule,
  NzSwitchModule,
  NzPopconfirmModule,
  NzTableModule,
  NzToolTipModule,
  NzMessageModule,
  NzBreadCrumbModule,
  NzBackTopModule,
  NzCardModule,
  NzSelectModule,
  NzCheckboxModule,
  NzDividerModule,
  NzInputNumberModule
];

@NgModule({
  declarations: [
    DateReadablePipe,
    MusicTypeReadablePipe,
    Base64DecoderPipe
  ],
  imports: [...modules, NzIconModule.forRoot(icons)],
  exports: [...modules, DateReadablePipe, MusicTypeReadablePipe, Base64DecoderPipe]
})
export class SharedModule {
}
