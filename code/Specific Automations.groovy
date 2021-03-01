/**
 *  Specific Automations
 *
 *  Copyright 2021 Lukas Weier
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Specific Automations",
    namespace: "sajiko5821",
    author: "Lukas Weier",
    description: "A specific App to control the rest",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	page(name: "mainPage", title:"Select", install: true, uninstall: true, submitOnChange: true){
    	section("Guest Mode"){
        	input "guestMode", "capability.switch", title: "Select Guest-Mode Switch", requred: false
            input "guestModeOff", "time", title: "When turn off", required: false
        }
    	
        section("Fridge"){
            input "kühlSwitch", "capability.switch", title: "Select Fridge", required: false
            input "kühlOnTime", "time", title: "When turn on", required: false
            input "kühlOffTime", "time", title: "When turn off", required: false
        }
        
        section("Livingroom Front"){
        	input "livingFront", "capability.switch", title: "Select Livingroom Front", required: false
            input "livingFrontLights", "capability.switch", title: "Select Livingroom Front Lights", multiple: true, required: false
            input "livingOffTime", "time", title: "When turn off", required: false
            input "livingOnTime", "time", title: "When turn on", required: false
     	}
        section("Welcome Back"){
        	input "frontDoor", "capability.contactSensor", title: "Select Door Sensor", required: false
            input "backLights", "capability.switch", title: "Which lights should turn on, when opening Front Door", multiple: true, required: false
        }
  	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	schedule(kühlOnTime, kühlOnHandler)
    schedule(kühlOffTime, kühlOffHandler)
    schedule(livingOnTime, livingOnHandler)
    schedule(livingOffTime, livingOffHandler)
    subscribe(guestMode, "switch.on", guestModeOffTimeHandler)
    subscribe(frontDoor, "contact.open", frontDoorOpenHandler)
    subscribe(location, "mode", modeChangeHandler)
}

def guestModeOffTimeHandler(evt){
	runOnce(guestModeOff, guestModeOffHandler)
}

def guestModeOffHandler(evt){
	guestMode.off()
}

def kühlOnHandler(evt){
	if(location.mode != "Away"){
    	kühlSwitch.on()
    }
}

def kühlOffHandler(evt){
	if(location.mode != "Away"){
    	kühlSwitch.off()
    }
}

def livingOnHandler(evt){
	if(location.mode != "Away" && guestMode.currentSwitch == "off"){
    	livingFront.on()
        runIn(5, livingFrontLightsOffHandler)
    }
}

def livingFrontLightsOffHandler(evt){
	livingFrontLights.off
}

def livingOffHandler(evt){
	if(location.mode != "Away" && guestmode.currentSwitch == "off"){
    	livingFront.off()
   	}
}

def frontDoorOpenHandler(evt){
    if(guestMode.currentSwitch == "off"){
    	livingOnHandler()
   	}
    if(location.mode == "Night"){
    	backLights.on()
  	}
}

def modeChangeHandler(evt){
	def beerTime = timeOfDayIsBetween(kühlOnTime, kühlOffTime, new Date(), location.timeZone)
	def livingNightTime = timeOfDayIsBetween(livingOffTime, livingOnTime, new Date(), location.timeZone)
    
    if(beerTime){
    	kühlOnHandler()
    }
}