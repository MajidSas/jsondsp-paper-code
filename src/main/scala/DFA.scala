/*
 * Copyright 2020 University of California, Riverside
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.ucr.cs.bdlab

import scala.collection.mutable.ArrayBuffer


class State(val stateType: String,
            val value: String,
          ) {
  var level: Int = 0;

  override def toString : String = {
    if(stateType.equals("array")) {
      return "State type: array"
    }

    "State type: %s, Value: %s".format(
      stateType,
      value
      // skip,
      // accept
    )
  }
}

class DFA() {
  var states = new Array[State](0)
  var tokens = new Array[(String,Int)](0)
  var currentState = 0

  def this(pathTokens: Array[String]) {
    this()
    var i = 0;
    val states = new ArrayBuffer[State](pathTokens.length)
    val tokens = new ArrayBuffer[(String, Int)](pathTokens.length)
    while (i < pathTokens.length) {
      val token = pathTokens(i)
      if(token.equals("$")) {
        states.append(new State("token", token.toString))
      } else if(token.equals("[*]")) {
        states.append(new State("array", ","))
      } else if(token.startsWith("..")) {
        states.append(new State("descendant", pathTokens(i).substring(2)))
      } else {
        states.append(new State("token", token.toString))
        tokens.append((token, states.size))
      }
      i+=2
    }
    this.states = states.toArray
    this.tokens = tokens.toArray
  }

  def getStates()  : Array[State] = {
    states
  }

  def getTokens() : Array[String] = {
    tokens.map(t => t._1)
  }

  def getTokenStates(token : String) : Array[Int] = {
    val tokenStates = ArrayBuffer[Int]()
    for(elem <- tokens){
      if(elem._1.equals(token)){
        tokenStates.append(elem._2)
      }
    }
    tokenStates.toArray
  }

  def setState(stateId: Int) {
    currentState = stateId;
  }
  def toNextState() {
    currentState = currentState+1
  }
  def toNextStateIfArray(level : Int) : Boolean = {
    if(currentState < states.size) {
      if(states(currentState).stateType.equals("array")){
        currentState = currentState+1
        states(currentState).level = level
        return true
      }
    }
    return false
  }
  def toPrevState(level : Int) {
    if(states(currentState-1).level == level) {
      currentState = currentState-1
    }
  }
  def getCurrentState() : Int = {
    currentState
  }

  def getPrevStateLevel() : Int = {
    if(currentState == 0) {
      return -1
    } else {
      states(currentState-1).level
    }
  }

  def checkToken(input : String, level : Int) : String = {
    // println((input, level, states(currentState).stateType.equals("descendant"), 
    // input.equals(states(currentState).value), 
    // currentState == states.size))
    if(states(currentState).stateType.equals("descendant")) {
      if(level > states(currentState).level && input.equals(states(currentState).value)) {
        toNextState()
        if(currentState == states.size) {
          currentState -= 1
          return "accept"
        } else {
          states(currentState).level = level
        }
      }
      return "continue"
    } else {
      if(level == states(currentState).level+1 && input.equals(states(currentState).value)){
        toNextState()
        if(currentState == states.size) {
          currentState -= 1
          return "accept"
        } else {
          states(currentState).level = level
          return "continue"
        }
      }
      return "reject"
    }
  }

  def checkArray() : String = {
    if(currentState == states.size){
      if(states(currentState-1).stateType.equals("array")) {
        return "accept"
      }
    }
    "continue"
  }

  override def toString = {
    var s = "Current state = " + currentState + "\n"
    var i = 1
    for(state <- states){
      if(i == currentState) {
        s+= "> "
      }
      s += state.toString + "\n"
      i+=1
    }
    s
  }
}