import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {IndexComponent} from "./module/index/component/index/index.component";
import {ListComponent} from "./module/music/component/list/list.component";
import {AddComponent} from "./module/music/component/add/add.component";
import {EditComponent} from "./module/music/component/edit/edit.component";

const routes: Routes = [
  {
    path: '',
    component: IndexComponent,
    children: [
      {path: '', redirectTo: 'music/list', pathMatch: 'full'},
      {path: 'music/list', component: ListComponent},
      {path: 'music/add', component: AddComponent},
      {path: 'music/edit/:id', component: EditComponent},
      {path: '**', redirectTo: 'music/list', pathMatch: 'full'}
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
