import {Component, Input, Output, View, Attribute, EventEmitter, ChangeDetectionStrategy} from 'angular2/core';
import {Control, Validators, ControlGroup, CORE_DIRECTIVES, FormBuilder, FORM_DIRECTIVES} from 'angular2/common';
import {Dropdown, InputOption} from '../../../../../view/components/semantic/modules/dropdown/dropdown'

import {InputText} from "../../../semantic/elements/input-text/input-text";
import {InputDate} from "../../../semantic/elements/input-date/input-date";
import {ParameterDefinition} from "../../../../../api/util/CwInputModel";
import {CwDropdownInputModel} from "../../../../../api/util/CwInputModel";
import {CwComponent} from "../../../../../api/util/CwComponent";
import {ParameterModel} from "../../../../../api/rule-engine/Condition";
import {ServerSideFieldModel} from "../../../../../api/rule-engine/ServerSideFieldModel";
import {I18nService} from "../../../../../api/system/locale/I18n";
import {ObservableHack} from "../../../../../api/util/ObservableHack";
import {CwRestDropdownInputModel} from "../../../../../api/util/CwInputModel";
import {RestDropdown} from "../../../semantic/modules/restdropdown/RestDropdown";
import {Verify} from "../../../../../api/validation/Verify";

@Component({
  selector: 'cw-serverside-condition',
  directives: [FORM_DIRECTIVES, CORE_DIRECTIVES, RestDropdown, Dropdown, InputOption, InputText, InputDate],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<form>
  <div flex layout="row" class="cw-condition-component-body">

    <template ngFor #input [ngForOf]="_inputs" #islast="last">
      <div *ngIf="input.type == 'spacer'" flex class="cw-input cw-input-placeholder">&nbsp;</div>
      <cw-input-dropdown *ngIf="input.type == 'dropdown'"
                         flex
                         class="cw-input"
                         [hidden]="input.argIndex !== null && input.argIndex >= _rhArgCount"
                         [value]="input.value"
                         placeholder="{{input.placeholder | async}}"
                         [required]="input.required"
                         [allowAdditions]="input.allowAdditions"
                         [class.cw-comparator-selector]="input.name == 'comparison'"
                         [class.cw-last]="islast"
                         (change)="handleParamValueChange(input.name, $event)">
        <cw-input-option
            *ngFor="#opt of input.options"
            [value]="opt.value"
            [label]="opt.label | async"
            icon="{{opt.icon}}"></cw-input-option>
      </cw-input-dropdown>

      <cw-input-rest-dropdown *ngIf="input.type == 'restDropdown'"
                              flex
                              class="cw-input"
                              [value]="input.value"
                              [hidden]="input.argIndex !== null && input.argIndex >= _rhArgCount"
                              placeholder="{{input.placeholder | async}}"
                              optionUrl="{{input.optionUrl}}"
                              optionValueField="{{input.optionValueField}}"
                              optionLabelField="{{input.optionLabelField}}"
                              [required]="input.required"
                              [allowAdditions]="input.allowAdditions"
                              [class.cw-comparator-selector]="input.name == 'comparison'"
                              [class.cw-last]="islast"
                              (change)="handleParamValueChange(input.name, $event)">
      </cw-input-rest-dropdown>

      <div flex layout-fill layout="column" class="cw-input" [class.cw-last]="islast" *ngIf="input.type == 'text' || input.type == 'number'">
        <cw-input-text
            flex
            [placeholder]="input.placeholder | async"
            [ngFormControl]="input.control"
            [type]="input.type"
            [hidden]="input.argIndex !== null && input.argIndex >= _rhArgCount"
            #fInput="ngForm"
        ></cw-input-text>
        <div flex="50" [hidden]="!fInput.touched || fInput.valid" class="name cw-warn basic label">[Better Msgs Soon]</div>
      </div>

      <cw-input-date *ngIf="input.type == 'datetime'"
                     flex
                    layout-fill
                     class="cw-input"
                     [class.cw-last]="islast"
                     [placeholder]="input.placeholder | async"
                     [hidden]="input.argIndex !== null && input.argIndex >= _rhArgCount"
                     [value]="input.value"
                     (blur)="handleParamValueChange(input.name, $event)"></cw-input-date>


    </template>
  </div>
</form>`
})
export class ServersideCondition {

  @Input() componentInstance:ServerSideFieldModel
  @Output() change:EventEmitter<ServerSideFieldModel>

  private _inputs:Array<any>
  private _resources:I18nService

  private _rhArgCount:number

  constructor(fb:FormBuilder, resources:I18nService) {
    this._resources = resources;
    this.change = new EventEmitter();
    this._inputs = [];
  }


  ngOnChanges(change) {
    let paramDefs = null
    if( change.componentInstance ) {
      this._rhArgCount = null
      paramDefs = this.componentInstance.type.parameters
    }
    if (paramDefs) {
      let prevPriority = 0
      this._inputs = []
      Object.keys(paramDefs).forEach(key => {
        let paramDef = this.componentInstance.getParameterDef(key)
        let param = this.componentInstance.getParameter(key);
        if (paramDef.priority > (prevPriority + 1)) {
          this._inputs.push({type: 'spacer', flex: 40})
        }
        prevPriority = paramDef.priority
        let input = this.getInputFor(paramDef.inputType.type, param, paramDef)
        this._inputs.push(input)
      })

      let comparison
      let comparisonIdx = null
      this._inputs.forEach((input:any, idx) => {
        if(ServersideCondition.isComparisonParameter(input)) {
          comparison = input
          this.applyRhsCount(comparison.value)
          comparisonIdx = idx
        } else if(comparisonIdx !== null){
          if(this._rhArgCount !== null ){
            input.argIndex = idx - comparisonIdx - 1
          }
        }

      })
      if(comparison){
        this.applyRhsCount(comparison.value)
      }

    }
  }

  getInputFor(type:string, param, paramDef:ParameterDefinition):any {

    let i18nBaseKey = paramDef.i18nBaseKey || this.componentInstance.type.i18nKey
    /* Save a potentially large number of requests by loading parent key: */
    this._resources.get(i18nBaseKey).subscribe(()=> {})

    let input
    if (type === 'text' || type === 'number') {
      input = this.getTextInput(param, paramDef, i18nBaseKey)
      console.log("ServersideCondition", "getInputFor", type, paramDef)
    } else if (type === 'datetime') {
      input = this.getDateTimeInput(param, paramDef, i18nBaseKey)
    } else if (type === 'restDropdown') {
      input = this.getRestDropdownInput(param, paramDef, i18nBaseKey)
    } else if (type === 'dropdown') {
      input = this.getDropdownInput(param, paramDef, i18nBaseKey)
    }
    input.type = type;
    return input
  }

  private getTextInput(param, paramDef, i18nBaseKey:string) {
    let rsrcKey = i18nBaseKey + '.inputs.' + paramDef.key
    let placeholderKey = rsrcKey + '.placeholder'
    let vFns:Function[] = []
    let minLen = paramDef.inputType.dataType['minLength']
    if (minLen > 0) {
      vFns.push(Validators.required)
      vFns.push(Validators.minLength(minLen))
    }
    if(paramDef.inputType.dataType['maxValue']){
      var max = paramDef.inputType.dataType['maxValue']
      vFns.push((control:Control) => {
        let resp:any = null
        let val = Number.parseFloat(control.value)
        if(val > max){
          resp = {maxValue: max, actualValue: val }
        }
        return resp
      })
    }
    if(Verify.isNumber(paramDef.inputType.dataType['minValue'])){
      var min = paramDef.inputType.dataType['minValue']
      vFns.push((control:Control) => {
        let resp:any = null
        let val = Number.parseFloat(control.value)
        if(val < min){
            resp = {minValue: min, actualValue: val }
        }
        return resp
      })
    }

    let control = new Control(this.componentInstance.getParameterValue(param.key), Validators.compose(vFns))
    control.valueChanges.debounceTime(250).subscribe((value) => {
      this.handleParamValueChange(param.key, value)
    })
    return {
      name: param.key,
      placeholder: this._resources.get(placeholderKey, paramDef.key),
      control: control,
      required: paramDef.inputType.dataType['minLength'] > 0
    }
  }

  private getDateTimeInput(param, paramDef, i18nBaseKey:string) {
    let rsrcKey = i18nBaseKey + '.inputs.' + paramDef.key
    return {
      name: param.key,
      value: this.componentInstance.getParameterValue(param.key),
      required: paramDef.inputType.dataType['minLength'] > 0,
      visible: true
    }
  }

  private getRestDropdownInput(param, paramDef, i18nBaseKey:string) {
    let inputType:CwRestDropdownInputModel = <CwRestDropdownInputModel>paramDef.inputType;
    let rsrcKey = i18nBaseKey + '.inputs.' + paramDef.key
    let placeholderKey = rsrcKey + '.placeholder'

    let currentValue = this.componentInstance.getParameterValue(param.key)
    let input:any = {
      value: currentValue,
      name: param.key,
      placeholder: this._resources.get(placeholderKey, paramDef.key),
      optionUrl: inputType.optionUrl,
      optionValueField: inputType.optionValueField,
      optionLabelField: inputType.optionLabelField,
      minSelections: inputType.minSelections,
      maxSelections: inputType.maxSelections,
      required: inputType.minSelections > 0,
      allowAdditions: inputType.allowAdditions
    }
    if (!input.value) {
      input.value = inputType.selected != null ? inputType.selected : ''
    }
    return input
  }

  private getDropdownInput(param:ParameterModel, paramDef:ParameterDefinition, i18nBaseKey:string):CwComponent {
    let inputType:CwDropdownInputModel = <CwDropdownInputModel>paramDef.inputType;
    let opts = []
    let options = inputType.options;
    let rsrcKey = i18nBaseKey + '.inputs.' + paramDef.key
    let placeholderKey = rsrcKey + '.placeholder'
    if (param.key == 'comparison') {
      rsrcKey = 'api.sites.ruleengine.rules.inputs.comparison'
    }
    else {
      rsrcKey = rsrcKey + '.options'
    }

    let currentValue = this.componentInstance.getParameterValue(param.key)
    let needsCustomAttribute = currentValue != null

    Object.keys(options).forEach((key:any)=> {
      let option = options[key]
      if (needsCustomAttribute && key == currentValue) {
        needsCustomAttribute = false
      }
      let labelKey = rsrcKey + '.' + option.i18nKey
      // hack for country - @todo ggranum: kill 'name' on locale?
      if (param.key === 'country') {
        labelKey = i18nBaseKey + '.' + option.i18nKey + '.name'
      }

      opts.push({
        value: key,
        label: this._resources.get(labelKey, option.i18nKey),
        icon: option.icon,
        rightHandArgCount: option.rightHandArgCount
      })
    })

    if (needsCustomAttribute) {
      opts.push({
        value: currentValue,
        label: ObservableHack.of(currentValue)
      })
    }


    let input:any = {
      value: currentValue,
      name: param.key,
      placeholder: this._resources.get(placeholderKey, paramDef.key),
      options: opts,
      minSelections: inputType.minSelections,
      maxSelections: inputType.maxSelections,
      required: inputType.minSelections > 0,
      allowAdditions: inputType.allowAdditions,
    }
    if (!input.value) {
      input.value = inputType.selected != null ? inputType.selected : ''
    }
    return input
  }


  handleParamValueChange(name:string, value:any) {
    this.componentInstance.setParameter(name, value)
    this.change.emit(this.componentInstance)
    if(name == 'comparison'){
      this.applyRhsCount(value)
    }
  }

  private applyRhsCount(selectedComparison:string) {
    let comparisonDef = this.componentInstance.getParameterDef('comparison')
    let comparisonType:CwDropdownInputModel = <CwDropdownInputModel>comparisonDef.inputType
    let selectedComparisonDef = comparisonType.options[selectedComparison]
    this._rhArgCount = ServersideCondition.getRightHandArgCount(selectedComparisonDef)
  }

  private static getRightHandArgCount(selectedComparison) {
    let argCount = null
    if (selectedComparison) {
      argCount = Verify.isNumber(selectedComparison.rightHandArgCount)
          ? selectedComparison.rightHandArgCount
          : 1
    }
    return argCount
  }

  private static isComparisonParameter(input) {
    return input && input.name === 'comparison'
  }

  private static getSelectedOption(input, value) {
    let opt = null
    let optAry = input.options.filter((e)=> { return e.value == value })
    if(optAry && optAry.length === 1){
      opt = optAry[0]
    }
    return opt
  }

}


