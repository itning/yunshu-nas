import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NzTabsModule} from 'ng-zorro-antd/tabs';
import {NzLayoutModule} from 'ng-zorro-antd/layout';
import {NzMenuModule} from 'ng-zorro-antd/menu';
import {IconsProviderModule} from '../../icons-provider.module';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NzButtonModule} from 'ng-zorro-antd/button';
import {NzInputModule} from 'ng-zorro-antd/input';
import {NzFormModule} from 'ng-zorro-antd/form';
import {NzGridModule} from 'ng-zorro-antd/grid';
import {NzToolTipModule} from 'ng-zorro-antd/tooltip';
import {NzCollapseModule} from 'ng-zorro-antd/collapse';
import {NzPopconfirmModule} from 'ng-zorro-antd/popconfirm';
import {NzModalModule} from 'ng-zorro-antd/modal';
import {NzTypographyModule} from 'ng-zorro-antd/typography';
import {NzMessageModule} from 'ng-zorro-antd/message';
import {NzNotificationModule} from 'ng-zorro-antd/notification';
import {NzAutocompleteModule} from 'ng-zorro-antd/auto-complete';
import {NzSelectModule} from 'ng-zorro-antd/select';
import {NzUploadModule} from 'ng-zorro-antd/upload';
import {NzDatePickerModule} from 'ng-zorro-antd/date-picker';
import {NzProgressModule} from 'ng-zorro-antd/progress';
import {NzCheckboxModule} from 'ng-zorro-antd/checkbox';
import {NzSwitchModule} from 'ng-zorro-antd/switch';
import {NzTableModule} from "ng-zorro-antd/table";
import {DateReadablePipe} from '../../pipe/date-readable.pipe';
import {NzImageModule} from "ng-zorro-antd/image";
import {AppRoutingModule} from "../../app-routing.module";
import { MusicTypeReadablePipe } from '../../pipe/music-type-readable.pipe';

@NgModule({
  declarations: [
    DateReadablePipe,
    MusicTypeReadablePipe
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    NzTabsModule,
    NzLayoutModule,
    NzMenuModule,
    NzButtonModule,
    NzInputModule,
    NzFormModule,
    NzGridModule,
    NzToolTipModule,
    NzCollapseModule,
    NzPopconfirmModule,
    NzModalModule,
    NzTypographyModule,
    NzMessageModule,
    NzNotificationModule,
    NzAutocompleteModule,
    NzSelectModule,
    NzUploadModule,
    NzDatePickerModule,
    IconsProviderModule,
    NzProgressModule,
    NzCheckboxModule,
    NzSwitchModule,
    NzTableModule,
    NzImageModule,
    AppRoutingModule,
  ],
  exports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    NzTabsModule,
    NzLayoutModule,
    NzMenuModule,
    NzButtonModule,
    NzInputModule,
    NzFormModule,
    NzGridModule,
    NzToolTipModule,
    NzCollapseModule,
    NzPopconfirmModule,
    NzModalModule,
    NzTypographyModule,
    NzMessageModule,
    NzNotificationModule,
    NzAutocompleteModule,
    NzSelectModule,
    NzUploadModule,
    NzDatePickerModule,
    IconsProviderModule,
    NzProgressModule,
    NzCheckboxModule,
    NzSwitchModule,
    NzTableModule,
    DateReadablePipe,
    NzImageModule,
    AppRoutingModule,
    MusicTypeReadablePipe,
  ]
})
export class SharedModule {
}
