(ns clj-csv-ddb.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clj-csv-ddb.helmet_response :as hr]
            [clj-csv-ddb.util :as util :refer [pct maps-filtered-by-value data-gender-filter data-age-filter ages genders label]]
            [reagent.core :as reagent :refer [atom]]
            [react-vis :as rvis :refer [XYPlot YAxis HorizontalBarSeries]]
            [cljs-http.client :as http]
            [cljs.core.async :as async :refer [<!]]))

;; HTTP requests

(defn post-response!
  "Takes params map and calls API to add item based on params."
  [params]
  (go (let [response (<! (http/post "http://localhost:5000/"
                                    {:with-credentials? false
                                     :query-params params
                                     :accepts :json}))]
        (if (= (:status response) 200)
          (:body response)
          {:error "Error 500"}))))

(defn put-response!
  "Takes params map and calls API to update item based on id and params."
  [params id]
  (go (let [response (<! (http/put (str "http://localhost:5000/" id)
                                   {:with-credentials? false
                                    :query-params params
                                    :accepts :json}))]
        (if (= (:status response) 200)
          (:body response)
          {:error "Error 500"}))))

(defn get-response!
  "Takes params map and calls API to request items matching params, or all."
  [params]
  (go (let [response (<! (http/get "http://localhost:5000/"
                                   {:with-credentials? false
                                    :query-params params
                                    :accepts :json}))]
        (if (= (:status response) 200)
          (:body response)
          {:error "Error 500"}))))

(defn delete-response!
  "Takes params map and calls API to delete item by ID or with optional conditions."
  [params id]
  (go (let [response (<! (http/delete (str "http://localhost:5000/" id)
                                      {:with-credentials? false
                                       :query-params params
                                       :accepts :json}))]
        (if (= (:status response) 200)
          (:body response)
          {:error "Error 500"}))))

;; Side-effective functions

(defn update-sort!
  "Modifies sort-state atom by new-val."
  [sort-state new-val]
  (if (= new-val (:sort-val @sort-state))
    (swap! sort-state update-in [:ascending] not)
    (swap! sort-state assoc :ascending true))
  (swap! sort-state assoc :sort-val new-val))

(defn begin-edit!
  "Removes :editable from all rows and adds :editable to row in table-state."
  [table-state row]
  (swap! table-state #(util/none-editable @table-state))
  (swap! table-state #(util/replace-item (assoc row :editable true) @table-state)))

(defn cancel-edit!
  "Replaces editable row in table-state with row minus editable key."
  [table-state row]
  (swap! table-state
         #(util/replace-item (dissoc row :editable true) @table-state)))

(defn persist-edit!
  "Removes editable state from row and persists row data to db via PUT request."
  [table-state row]
  (cancel-edit! table-state @row)
  (put-response! @row (:vector @row)))

(defn add-new-row!
  "Adds new item with id new-id to table-state, resets sort to default putting new item at top of table,
  persists id and default fields to db, and opens row for editing."
  [table-state sort-state]
  (let [last-id (last (sort (map #(js/parseInt (subs (:vector %) 1)) @table-state)))
        new-id (str "v" (inc last-id))
        row {:vector new-id
             :ref_date 2002
             :geo "Canada"
             :uom "Percent"
             :uom_id 239
             :scalar_factor "Units"
             :scalar_id 0
             :decimals 0}]
    (swap! table-state #(conj @table-state row))
    (reset! sort-state {:sort-val :vector :ascending false})
    (post-response! row)
    (begin-edit! table-state row)))

(defn delete-row!
  "Deletes a row from db and modifies table state accordingly."
  [table-state id]
  (delete-response! {} id)
  (swap! table-state
         (fn [hr]
           (remove #(= (:vector %) id) hr))))

(defn hist-data-filter!
  "Modifies the h-state to provide new percentage values for responses."
  [t-s h-s gender age]
  (let [g (into [] (flatten (data-gender-filter t-s gender))) ; gender
        a-g (into [] (flatten (data-age-filter g age))) ;age-gender
        m maps-filtered-by-value
        k ':student_response ; ' means take as data, do not eval
        total (count a-g)
        donot (pct (count (m k "Do not ride a bicycle" a-g)) total)
        always (pct (count (m k "Always" a-g)) total)
        often (pct (count (m k "Often" a-g)) total)
        sometimes (pct (count (m k "Sometimes" a-g)) total)
        rarelynever (pct (count (m k "Rarely or never" a-g)) total)]
    (reset! h-s {:donot donot ; histogram-state
                 :always always
                 :often often
                 :sometimes sometimes
                 :rarelynever rarelynever})))

(defn filter-histogram!
  "Filters the table-state to provide data for histogram."
  [t h-state opts-coll age-filter gender-filter filter]
  (let [opts (map (fn [o] (-> o .-value)) opts-coll)]
    (cond
      (= filter "age") (swap! age-filter (fn [] opts))
      (= filter "gender") (swap! gender-filter (fn [] opts)))
    (hist-data-filter! @t h-state @gender-filter @age-filter)))

;; React components

(defn hr-input!
  "Returns an input field component for helmet-response table."
  [row c]
  (let [number? (some #(= (key c) %) hr/int-kws)]
    (println number?)
    (println (str (key c)))
    (println hr/int-kws)
  [:input {:name (str ((key c) row))
           :type (if number? "number" "text")
           :defaultValue ((key c) @row)
           :size (if (nil? ((key c) @row))
                   3
                   (count (str ((key c) @row))))
           :disabled (if (= (key c) :vector) true false)
           :on-change (fn [e]
                        (swap! row #(assoc @row (key c) (-> e .-target .-value))))}]))

(defn helmet-response-table!
  "Returns a sortable table component that requests helmet-response data.
  Any change to sort-state or table-state will cause this component to refresh."
  [f table-state]
  (let [sort-state (atom {:sort-val :vector :ascending false})]
    (fn [f]
      [:table {:class "table table-sm table-striped table-hover"}
       [:thead {:align "left"}
        [:tr {:class "columns"}
         (for [c hr/hr-keys-columns]
           [:th {:on-click #(update-sort! sort-state (key c))} (val c)])
         [:th {:on-click #(add-new-row! table-state sort-state) :class "material-icons add-button"} "add_box"]]]
       [:tbody
        (let [sorted-data (util/sorted-contents @sort-state @table-state)
              validated-data (map #(hr/helmet-response %) sorted-data)]
          (for [original-row (util/filter-content f validated-data)]
            (let [row (atom original-row)]
              ^{:key (:vector @row)}
              [:tr
               (for [c hr/hr-keys-columns]
                 [:td {:class (if (util/editable? @row) "editable" nil)}
                  (if (util/editable? @row)
                    [hr-input! row c]
                    ((key c) @row))]) ; else
               (if (util/editable? @row)
                 [:td {:class "row-buttons"}
                  [:i {:class "material-icons submit-button"
                       :on-click #(persist-edit! table-state row)} "check_circle"]
                  [:i {:class "material-icons cancel-button"
                       :on-click #(cancel-edit! table-state original-row)} "cancel"]]
                 [:td {:class "row-buttons"} ; else
                  [:i {:class "material-icons edit-button"
                       :on-click #(begin-edit! table-state @row)} "edit"]
                  [:i {:class "material-icons delete-button"
                       :on-click #(delete-row! table-state (:vector @row))} "delete_forever"]])])))]])))

(defn histogram!
  "Returns a React component containing a chart made with the react-vis library."
  []
  (let [age-filter (atom nil)
        gender-filter (atom nil)
        h-state (atom nil)]
    (fn [t]
      [:div {:class "hist-frame"}
       [:div {:class "selector"}
        [:select
         {:id "gender"
          :multiple true
          :on-change #(let [opts-coll (array-seq (-> % .-target .-selectedOptions))]
                        (filter-histogram! t h-state opts-coll age-filter gender-filter "gender"))}
         (for [x (genders t)]
           [:option x])]
        [:select
         {:id "age"
          :multiple true
          :on-change #(let [opts-coll (array-seq (-> % .-target .-selectedOptions))]
                        (filter-histogram! t h-state opts-coll age-filter gender-filter "age"))}
         (for [x (sort (ages t))]
           [:option x])]]
       [:h3 {:class "question"}
        "How often do you wear your bicycle helmet while cycling?"]
       [:div {:class "histogram"}
        [:> XYPlot
         {:width 720
          :height 360
          :margin {:left 225}
          :color-type "category"
          :color-domain [0 1 2 3 4]
          :color-range ["#97D4CE" "#66CAD8" "#4AC4F4" "#4ABDAA" "#2B69B3"]
          :y-type "ordinal"}
         [:> YAxis {:tick-total 5}]
         [:> HorizontalBarSeries
          {:data [{:y (label "Do not ride a bicycle" :donot @h-state)
                   :x (:donot @h-state) :color 0}
                  {:y (label "Always" :always @h-state)
                   :x (:always @h-state) :color 1}
                  {:y (label "Often" :often @h-state)
                   :x (:often @h-state) :color 2}
                  {:y (label "Sometimes" :sometimes @h-state)
                   :x (:sometimes @h-state) :color 3}
                  {:y (label "Rarely or never" :rarelynever @h-state)
                   :x (:rarelynever @h-state) :color 4}]}]]]])))

(defn table-filter!
  "Returns a filter component that returns a table component."
  []
  (let [filter-value (atom nil)
        table-state (atom nil)]
    (go (reset! table-state (<! (get-response! {}))))
    (fn []
      [:div {:class "frame"}
       [:h1 {:id "title"} "CST8333 Final Project"]
       [:h2 {:id "subtitle"} "Sam Heaton 040917452"]
       [histogram! table-state]
       [:div {:class "filter"} "Filter: "
        [:input {:type "text"
                 :value @filter-value
                 :on-change #(reset! filter-value (-> % .-target .-value))}]
        [:span {:class "student-name"} "Sam Heaton 040917452"]]
       [helmet-response-table! @filter-value table-state]])))

;; App entry point

(defn start
  "Entry point into the application calls table-filter."
  []
  (reagent/render-component [table-filter!]
                            (. js/document (getElementById "app"))))

(defn ^:export init []
  ;; init is called ONCE when the page loads
  ;; this is called in the index.html and must be exported
  ;; so it is available even in :advanced releas builds
  (start))

(defn stop []
  ;; stop is called before any code is reloaded
  ;; this is controlled by :before-load in the config
  (js/console.log "stop"))
