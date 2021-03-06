(ns metasolfeggio.chords
  (:use [overtone.live]
        [overtone.inst sampled-piano]))

(def metro (metronome 40))

(def SCALE_CHORDS
  {
   :major
   [
    :major      ; I
    :minor      ; ii
    :minor      ; iii
    :major      ; IV
    :major      ; V
    :minor      ; vi
    :diminished ; viio
    ]

   :minor
   [
    :minor      ; i
    :diminished ; iio
    :major      ; III
    :minor      ; iv
    :major      ; V
    :major      ; VI
    :diminished ; viio
    ]
   }
  )

(defn roman->chord [root scale roman]
  [(find-note-name (+ (note root)
                      (degree->interval roman scale)
                      (if (and (= roman 7) (= scale :minor)) 1 0)))
   (nth (scale SCALE_CHORDS) (dec roman))])

(defn progression [root scale romans]
  (map #(roman->chord root scale %) romans))

;(def beat-offsets [0 0.1 0.2 1/3 0.7 0.9])
;(def beat-offsets [0 0 0 0 0 0])
(def beat-offsets [0 0.1 0.11 0.13 0.15 0.17 0.2 0.4 0.5 0.55 0.6 0.8])

(defn play-notes[metro beat notes offsets]
  (dorun
   (map (fn [note offset]
          (at (metro (+ beat offset)) (sampled-piano note 0.3)))
        notes
        offsets)))

(def max-range 24)
(def range-variation 12)
(def range-period 8)

(defn play-chord [metro beat [chord-root chord-name]]
  (let [note-range (cosr beat range-variation max-range range-period)
        notes (rand-chord chord-root chord-name (count beat-offsets) note-range)]
   (play-notes metro beat notes beat-offsets)))

(def harmony
  (sorted-map
   1 [3 6 2 4 5]
   2 [5 7]
   3 [6]
   4 [5 7]
   5 [1]
   6 [2 4]
   7 [1]
  )
)

(defn harmony->progression [roman]
  (if (and (= roman 1) (= (rand-int 2) 0))
    '(1)
    (cons roman
          (harmony->progression (choose (get harmony roman))))))

(def chord-progs
  [
   [1]
   [2 4]
   [5 7]
   [6]
   [2 4]
   [5 7]
   [1]
  ]
)

(defn play-progression [metro beat p]
  (when p
    (println (first p))
    (play-chord metro beat (first p))
    (apply-at (metro (inc beat)) #'play-progression [metro (inc beat) (rest p)])))

(comment
  (play-progression metro (metro) (progression :C4 :major (map choose chord-progs)))
  (play-progression metro (metro) (progression :C4 :minor (map choose chord-progs)))
  (play-progression metro (metro) (progression :C4 :minor (harmony->progression 1)))
  (stop))
