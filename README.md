MaskedImageView
===============
Masked ImageView for Android. Easily apply any mask to an ImageView.

Setup
=====
There are 2 ways you can use this library. Both involve cloning this project.

One, build the library jar by runnning ```./gradlew jar``` from the root folder. This will create ```MaskedImageView.jar``` in the ```library/build/libs/``` folder. Add this jar to your project dependencies.

Two, add the library as an Android Library to your project. You can find all the library files in the [library](library) folder.

Usage
=====
Using this is super simple.

1. Make sure to define a custom XML namespace, ```xmlns:custom="http://schemas.android.com/apk/res-auto"```.
1. Add an ```<nl.ansuz.android.maskedimageview.widget.MaskedImageView>``` tag to your layout and configure it the same way you would with any ```ImageView```.
1. Add the mask to the ```MaskedImageView``` by adding the following attribute: ```custom:mask="@drawable/some_mask"```.

That's all there is to it. If something is not entirely clear, have a look at the [Example Project](example/).

