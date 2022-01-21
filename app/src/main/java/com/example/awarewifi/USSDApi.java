package com.example.awarewifi;

/*
 * Copyright (c) 2020. BoostTag E.I.R.L. Romell D.Z.
 * All rights reserved
 * porfile.romellfudi.com
 */


import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Romell Dominguez
 * @version 1.1.c 13/02/2018
 * @since 1.0.a
 */
public interface USSDApi {
    void send(String text, USSDController.CallbackMessage callbackMessage);
    void cancel();
    void callUSSDInvoke(String ussdPhoneNumber, HashMap<String,HashSet<String>> map,
                        USSDController.CallbackInvoke callbackInvoke);
    void callUSSDInvoke(String ussdPhoneNumber, int simSlot, HashMap<String,HashSet<String>> map,
                        USSDController.CallbackInvoke callbackInvoke);
    void callUSSDOverlayInvoke(String ussdPhoneNumber, HashMap<String,HashSet<String>> map,
                               USSDController.CallbackInvoke callbackInvoke);
    void callUSSDOverlayInvoke(String ussdPhoneNumber, int simSlot, HashMap<String,HashSet<String>> map,
                               USSDController.CallbackInvoke callbackInvoke);
}