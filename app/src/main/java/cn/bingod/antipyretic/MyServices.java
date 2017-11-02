package cn.bingod.antipyretic;

import android.content.Intent;

import java.util.List;

/**
 * @author bin
 * @since 2017/7/10
 */

public interface MyServices {

    @Uri("/main2")
    boolean main2();

    @Uri("/main2")
    boolean main2(@Query("p") String p);

    @Uri("/main2")
    boolean main2(@Query("p") String a, @Extra("e") String b, @Extra("ee") String[] c
            , @Extra("a") int d, @Extra("b") boolean e, @Extra("c") float f
            , @Extra("d") long g, @Extra("f") double h, @Extra("f") double i);

    @Uri("/main2/{id}")
    boolean main22(@Path("id") String id, @Query("p") String p);

    @Uri("/main2")
    boolean main3(@Extra("id") String id);

    @Uri("/main2")
    boolean main33(@Extra("id") int id);

    @Uri("/main2")
    boolean main333(@Extra("id") boolean id);

    @Uri("/main2")
    boolean main3333(@Extra("id") short id);

    @Uri("/main2")
    boolean main33333(@Extra("id") long id);

    @Uri("/main2")
    boolean main3333(@Extra("id") double id);

    @Uri("/main2")
    boolean main3333(@Extra("id") float id);

    @Uri("/main2")
    boolean main3333(@Extra("id") CharSequence id);

    @Uri("/main2")
    boolean main3333(@Extra("id") List<Integer> id);

    @Uri("/main2")
    @ForResult(requestCode = 119)
    void openTestForResult(@Extra("testObj") TestObj obj, @RequestCode() int value);

    @Uri("com.mogoroom.antipyretics.action.main2/{id}")
    boolean main4(@Query("p") String p, @Extra("testObj") TestObj obj);

    @Uri("/main2")
    @Transition(enterAnim = R.anim.activity_open_in_bottom, exitAnim = 0)
    void openTestWithAnim(@Query("p") String id, @Extra("e") String extra, @Extra("testObj") TestObj obj);

    @Uri("/web")
    @Flags({Intent.FLAG_ACTIVITY_CLEAR_TOP, Intent.FLAG_ACTIVITY_NEW_TASK})
    boolean toWeb(@Query("url") String url);

    @Uri("/blank/{id}")
    @Flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    BlankFragment toBlank(@Path("id") String a, @Query("param") String b, @Extra("extra") TestObj obj);
}
