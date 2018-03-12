<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <title>index</title>
        <script>
          function executeCMD(str) {
              var xreq;
 
              if (str == "") {
                  document.getElementById("showtext").innerHTML = "<p>Enter a value!</p>";
                  return;
              }
              if (window.XMLHttpRequest) {
                  xreq = new XMLHttpRequest();
              }
              else {
                  xreq = new ActiveXObject("Microsoft.XMLHTTP");
              }
              xreq.onreadystatechange = function () {
                  if (xreq.readyState == 4) {
                      if (xreq.status == 200) {
                         document.getElementById("showtext").innerHTML = xreq.responseText;
                      } else {
                         document.getElementById("showtext").innerHTML = "<p>something went wrong with the request</p>";
                      }
                  }
              }
 
              xreq.open("get", "getRequest.jsp?q=" + str, "true");
              xreq.send();
              document.getElementById("showtext").innerHTML = "<p>request has been submitted-waiting for answer</p>";
          }
        </script>
    </head>
    <body>
        <h1>A test for Application Continuity</h1>
          
        <form>
            <table>
                <tr>
                    <td>Enter a message to be sent asynchronously to the database</td>
                    <td>
                        <input type="text" id="messageField"/>
                    </td>
                    <td>
                        <input type="button" value="Submit"
                               onclick="executeCMD(document.getElementById('messageField').value)"/>
                    </td>
                </tr>
            </table>
        </form>
        <p>The processing will be performed asynchronously</p>
        <div id="showtext">request has not yet been submitted</div>
    </body>
</html>
