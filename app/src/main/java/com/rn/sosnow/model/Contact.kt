package com.rn.sosnow.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.rn.sosnow.BR

class Contact : BaseObservable(){
    @Bindable
    var id: String = ""
        set(value){
            field = value
            notifyPropertyChanged(BR.id)
        }
    @Bindable
    var coverUrl: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.coverUrl)
        }
    @Bindable
    var name: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }
    @Bindable
    var number: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.number)
        }

}