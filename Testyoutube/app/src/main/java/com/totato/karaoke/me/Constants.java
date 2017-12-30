/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.totato.karaoke.me;

public interface Constants {

    public static final String ADD ="@ADD@";
    public static final String DEL ="@DEL@";
    public static final String NEXT ="@NEX@";
    public static final String PLAY ="@PLA@";
    public static final String STOP ="@STO@";
    public static final String QUIT ="@QUI@";
    public static final String PRIO ="@PRI@";
    public static final String DIV ="@@@@@";

    String STATE_FIREBASE_OK = "OK";
    String STATE_FIREBASE_BUSY = "BUSY";
    enum STATE {
        ON_INIT,
        ON_INIT_FAIL,
        ON_WAIT_ADD_VIDEO,
        ON_LOADING_VIDEO,
        ON_LOADED_VIDEO,
        ON_STARTED_VIDEO,
        ON_ERROR_VIDEO,
        ON_STARTED_AD,
        ON_ENDED_VIDEO,
    }
    enum PAGE{
        LIST_WAITING,
        SEARCH_ONLINE,
        SEARCH_OFFLINE
    }
}
