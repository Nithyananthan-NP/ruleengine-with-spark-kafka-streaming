package com.peerislands_space.insuranceprocess
import java.beans.BeanProperty

case class Insurance(@BeanProperty var name:String, @BeanProperty var age:Long,
                     @BeanProperty var hasIncident:Boolean, @BeanProperty var address:String, @BeanProperty var insuranceId:Long, @BeanProperty var premium:Double)



