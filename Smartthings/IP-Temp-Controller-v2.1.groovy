/**
 *  IP Temp Controller v2.1
 *
 *  Copyright 2016 Mark Adkins
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
metadata {
	definition (name: "IP Temp Controller v2.1", namespace: "markeadkins", author: "Mark Adkins") {
		
        capability "Refresh"
        capability "Polling"
        capability "TemperatureMeasurement"
        
        command "refresh"
        command "lowerSetPoint"
        command "raiseSetPoint"
        command "setTempTo"
        command "setTempTo30"
        command "setTempTo40"
        command "setTempTo50"
        command "setTempTo60"
        command "setTempTo70"
        command "setTempTo80"

        attribute "setPoint", "number"
        attribute "status", "string"
        attribute "temperature", "number"
        attribute "tempSetPoint", "number"
        attribute "oldTempSetPoint", "number"
	}
    
    preferences {
    	input("DeviceIP", "string", title:"Device IP Address", description: "Please enter your device's IP Address", required: true, displayDuringSetup: true)
		input("DevicePort", "string", title:"Device Port", description: "Please enter your device's Port", required: true, displayDuringSetup: true)
    }

	tiles {
		valueTile("temperature", "device.temperature", inactiveLabel: false, width: 2, height: 2) {
			state "temperature", label:'${currentValue}°',
				backgroundColors:[
					[value: 31, color: "#153591"],
					[value: 44, color: "#1e9cbb"],
					[value: 59, color: "#90d2a7"],
					[value: 74, color: "#44b621"],
					[value: 84, color: "#f1d801"],
					[value: 95, color: "#d04e00"],
					[value: 96, color: "#bc2323"]
				]
		}
        
        standardTile("upButtonControl", "device.tempSetPoint", inactiveLabel: false, decoration: "flat") {
			state "setpoint", action:"raiseSetPoint", icon:"st.thermostat.thermostat-up"
		}
        
        standardTile("downButtonControl", "device.tempSetPoint", inactiveLabel: false, decoration: "flat") {
			state "setpoint", action:"lowerSetPoint", icon:"st.thermostat.thermostat-down"
		}

		valueTile("tempSetPoint", "device.tempSetPoint", inactiveLabel: false, width: 1, height: 1) {
			state "tempSetPoint", label:'${currentValue}°'
		}
        
		valueTile("status", "device.status", inactiveLabel: false, width: 1, height: 1) {
			state "status", label:'${currentValue}'
		}
        
		standardTile("refresh", "device.switch", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false) {
			state("default", label:'refresh', action:"refresh.refresh", icon:"st.secondary.refresh-icon")
		}
        
        valueTile("30", "device.tempSetPoint", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
			state("default", label:'30', action:"setTempTo30")
		}
        
        valueTile("40", "device.tempSetPoint", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
			state("default", label:'40', action:"setTempTo40")
		}
        
        valueTile("50", "device.tempSetPoint", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
			state("default", label:'50', action:"setTempTo50")
		}
        
        valueTile("60", "device.tempSetPoint", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
			state("default", label:'60', action:"setTempTo60")
		}
        
        valueTile("70", "device.tempSetPoint", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
			state("default", label:'70', action:"setTempTo70")
		}
        
        valueTile("80", "device.tempSetPoint", width: 1, height: 1, inactiveLabel: false, canChangeIcon: false, decoration: "flat") {
			state("default", label:'80', action:"setTempTo80")
		}
        
        main("temperature")
        
        details(["temperature", "upButtonControl", "tempSetPoint", "refresh", "status", "downButtonControl", "30", "40", "50", "60", "70", "80"])
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
	//log.debug "Updated with settings: ${settings}"
	//initialize()
}

def initialize() {
	log.debug "Initialized with settings: ${settings}"
    refresh()
}

def parse(String description) {
	log.debug "Parsing '${description}'"
	def msg = parseLanMessage(description)
	def json = msg.json
    
    log.debug "The returned temp is: ${json.object.temp}"
    log.debug "The returned tempSetPoint is: ${json.object.tempSetPoint}"
    log.debug "The returned status is: ${json.object.status}"
    
    sendEvent(name: "temperature", value: json.object.temp as Double)
    sendEvent(name: "tempSetPoint", value: json.object.tempSetPoint as Double)
    sendEvent(name: "status", value: json.object.status as String)
}

def refresh() {
	log.debug "Executing 'refresh'"
	runCmd("Refresh")
}

def lowerSetPoint() {
	setTempTo(device.currentValue("tempSetPoint") - 1)
}

def raiseSetPoint() {
	setTempTo(device.currentValue("tempSetPoint") + 1)
}

def setTempTo(Double newSetTemp) {
	log.debug "setTempTo was triggered to ${newSetTemp} degrees"
    
    sendEvent(name: "oldTempSetPoint", value: device.currentValue("tempSetPoint") as Double)
    
    log.debug "Old Set Temp is: ${device.currentValue("oldTempSetPoint")}"
    
    sendEvent(name: "tempSetPoint", value: newSetTemp as Double)
    
    log.debug "New Set Temp is: ${device.currentValue("tempSetPoint")}"
    
	runCmd()
}

def setTempTo30() {
	setTempTo(30)
}

def setTempTo40() {
	setTempTo(40)
}

def setTempTo50() {
	setTempTo(50)
}

def setTempTo60() {
	setTempTo(60)
}

def setTempTo70() {
	setTempTo(70)
}

def setTempTo80() {
	setTempTo(80)
}

def runCmd(String varCommand) {
	def host = DeviceIP
	def hosthex = convertIPtoHex(host).toUpperCase()
	def porthex = convertPortToHex(DevicePort).toUpperCase()
	device.deviceNetworkId = "$hosthex:$porthex"
    def path = "/json"
    
    if ((device.currentValue("tempSetPoint") != device.currentValue("oldTempSetPoint")) && (varCommand != "Refresh")) 
    {
    	path+="?temp=${device.currentValue("tempSetPoint")}"
        sendEvent(name: "oldTempSetPoint", value: device.currentValue("tempSetPoint") as Double)
    }
    
    def body = ""
    def headers = [:]
    headers.put("HOST", device.deviceNetworkId)
    headers.put("Content-Type", "application/json")    
    log.debug "The Header is ${headers}"
    def method = "GET"
    
    try {
		def hubAction = new physicalgraph.device.HubAction(
			method: method,
			path: path,
			body: body,
			headers: headers
			)
		log.debug hubAction
		return hubAction
	}
	catch (Exception e) {
		log.debug "Hit Exception $e on $hubAction"
	} 
}

private String convertIPtoHex(ipAddress) {
	String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
	return hex
}
private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
	return hexport
}
private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}
private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}