Smiley Kotlin App (Niko Niko)
=============================

This is a learning project undertaken by the TIM team as a vehicle for learning Kotlin.

Tasks
-----

* Deal with aggregating events into users happiness.
  * If they submit happiness twice (on the same day), GET should return just the last one.
  * See existing ignored test in RecordHSIT
* Support users' happiness on specific dates.
* Refactor to have an EventCodecs class like most other apps.
  * Should have something like EventCodecs.serialise() and EventCodecs.deserialise() and classes for each event
  * Currently the event is just the emotion, but adding date support will require some sort of structure
* Use JAX-RS instead of directly writing a servlet.
* Pretty dashboard of users' happiness over the last few days
* Periodically send an email (towards the end of each working day), prompting users to enter their happiness
