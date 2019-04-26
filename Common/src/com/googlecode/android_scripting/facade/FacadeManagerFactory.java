/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.googlecode.android_scripting.facade;

import android.app.Service;
import android.content.Intent;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManagerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FacadeManagerFactory implements RpcReceiverManagerFactory {

    private final int mSdkLevel;
    private final Service mService;
    private final Intent mIntent;
    private final Collection<Class<? extends RpcReceiver>> mClassList;
    private final Map<String, RpcReceiverManager> mFacadeManagers;

    public FacadeManagerFactory(int sdkLevel, Service service, Intent intent,
                                Collection<Class<? extends RpcReceiver>> classList) {
        mSdkLevel = sdkLevel;
        mService = service;
        mIntent = intent;
        mClassList = classList;
        mFacadeManagers = new HashMap<>();
    }

    @Override
    public FacadeManager create(String sessionId) throws IllegalArgumentException {
        FacadeManager facadeManager = new FacadeManager(mSdkLevel, mService, mIntent, mClassList);
        // TODO(markdr): This lock isn't entirely safe, since another object injects this reference
        // into the RpcReceiverManagerFactory, and can therefore make mutations elsewhere.
        // Refactor this to prevent that from potentially occurring.
        synchronized (mFacadeManagers) {
            if (mFacadeManagers.containsKey(sessionId)) {
                throw new IllegalArgumentException("SessionID " + sessionId + " already exists.");
            }
            mFacadeManagers.put(sessionId, facadeManager);
        }
        return facadeManager;
    }

    @Override
    public boolean destroy(String sessionId) {
        RpcReceiverManager removed = mFacadeManagers.remove(sessionId);
        if (removed != null) {
            removed.shutdown();
        }
        return removed != null;
    }

    @Override
    public Map<String, RpcReceiverManager> getRpcReceiverManagers() {
        return mFacadeManagers;
    }
}
