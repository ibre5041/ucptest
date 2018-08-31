package sandbox.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

//this makes jackson not to include null values in JSON
@JsonInclude(Include.NON_NULL)
public class ResponseObj {
        private String result = "OK";
        private String error;
        private String retorno;
        public String getResult() {
                return result;
        }
        public void setResult(String result) {
                this.result = result;
        }
        public String getError() {
                return error;
        }
        public void setError(String error) {
                this.error = error;
        }
        public String getRetorno() {
                return retorno;
        }
        public void setRetorno(String retorno) {
                this.retorno = retorno;
        }
}
