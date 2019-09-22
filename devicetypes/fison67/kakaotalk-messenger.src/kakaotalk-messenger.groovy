/**
 *  KakaoTalk Messenger (v.0.0.2)
 *
 * MIT License
 *
 * Copyright (c) 2019 fison67@nate.com
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
*/

import groovy.json.*

include 'asynchttp_v1'

metadata {
	definition (name: "KakaoTalk Messenger", namespace: "fison67", author: "fison67") {
        capability "Speech Synthesis"
        capability "Actuator"
	}

	simulator {}

	tiles {
    	multiAttributeTile(name:"status", type: "generic", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
				attributeState("status", label:'${currentValue}', backgroundColor:"#00a0dc")
			}
		}
    }
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def setInfo(String app_url) {
	log.debug "${app_url}"
	state.app_url = app_url
}

def speak(text){
	log.debug "Speak :" + text
    sendData(text)
}

def updated() {}

def sendData(text){
    parent.saveData(text)
    
	_sendData(makeParam("me", text))
    if(parent.getFriendsUUID().size() > 0){
		_sendData(makeParam("friends", text))
    }
}

def _sendData(data){
    try {
        httpPost(data) { resp ->
            if(resp.data.result_code == 0){
            	log.debug "Success to send Message}"
            } else if(resp.data.successful_receiver_uuids){
            	log.debug "Success to send Message}"
            } else{
            	log.debug "Failed to send Message >> ${resp.data}"
            }
        }
    } catch (e) {
        log.debug "something went wrong: $e"
    }
}

def makeParam(type, text){
	def body = [
    	"object_type": "text",
        "text": text,
        "link": [
            "web_url": parent.getMessageCheckURL(),
            "mobile_web_url": parent.getMessageCheckURL()
         ]
    ]
    
	def params = []
	switch(type){
    case "me":
    	params = [
            uri: "https://kapi.kakao.com/v2/api/talk/memo/default/send",
            headers: [
                'Authorization': 'Bearer ' + parent.getAccessToken(),
          		'content-type' : 'application/x-www-form-urlencoded'
            ],
            body: [
                "template_object": JsonOutput.toJson(body).replace(">>", "\\n")
            ]
        ]
    	break
    case "friends":
    	params = [
            uri: "https://kapi.kakao.com/v1/api/talk/friends/message/default/send",
            headers: [
                'Authorization': 'Bearer ' + parent.getAccessToken(),
		'content-type' : 'application/x-www-form-urlencoded'
            ],
            body: [
                "receiver_uuids": JsonOutput.toJson(parent.getFriendsUUID()),
                "template_object": JsonOutput.toJson(body).replace(">>", "\\n")
            ]
        ]
    	break
    }
    return params
}
