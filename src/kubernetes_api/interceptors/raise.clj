(ns kubernetes-api.interceptors.raise)

(defn status-error? [status]
  (>= status 400))

(def ^:private error-type->status-code
  {:bad-request                   400
   :invalid-input                 400
   :unauthorized                  401
   :payment-required              402
   :forbidden                     403
   :not-found                     404
   :method-not-allowed            405
   :not-acceptable                406
   :proxy-authentication-required 407
   :timeout                       408
   :conflict                      409
   :gone                          410
   :length-required               411
   :precondition-failed           412
   :payload-too-large             413
   :uri-too-long                  414
   :unsupported-media-type        415
   :range-not-satisfiable         416
   :expectation-failed            417
   :unprocessable-entity          422
   :locked                        423
   :upgrade-required              426
   :too-many-requests             429
   :server-error                  500
   :not-implemented               501
   :bad-gateway                   502
   :service-unavailable           503
   :gateway-timeout               504
   :http-version-not-supported    505})

(def status-code->error-type (zipmap (vals error-type->status-code) (keys error-type->status-code)))

(defn raise-exception [{:keys [body status] :as _response}]
  (throw (ex-info "API-Server error"
                  {:type (status-code->error-type status)
                   :body body})))

(defn new [_]
  {:name  ::raise
   :leave (fn [{:keys [request response] :as _context}]
            (if (status-error? (:status response))
              (raise-exception response)
              (with-meta {:response (:body response)}
                         {:request request :response response})))})
