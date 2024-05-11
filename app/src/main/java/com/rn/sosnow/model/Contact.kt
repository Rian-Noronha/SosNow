package com.rn.sosnow.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.rn.sosnow.BR
import java.io.Serializable

class Contact : BaseObservable(), Serializable{
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
    var description: String = ""
        set(value){
            field = value
            notifyPropertyChanged(BR.name)
        }
    @Bindable
    var address: String = ""
        set(value){
            field = value
            notifyPropertyChanged(BR.name)
        }
    @Bindable
    var destinationLatitude: String = ""
        set(value){
            field = value
            notifyPropertyChanged(BR.name)
        }

    @Bindable
    var destinationLongitude: String = ""
        set(value){
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