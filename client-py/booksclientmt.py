'''
Created on Mar 1, 2018

@author: ivabrezi
'''

import requests
import json
import string
import random
import threading
import logging

logging.basicConfig(level=logging.CRITICAL,
                    format='[%(levelname)s] (%(threadName)-10s) %(message)s',
                    )

def updateAll():
    logging.info('Starting')
    # We talk in JSON
    headers = {'Content-type': 'application/json', 'Accept': 'application/json'}

    # Replace with the correct URL

    url = "http://localhost:8080/tomcat-dbcp-rest/books/"

    getAllResponse = requests.get(url, headers=headers)
    print (getAllResponse.status_code)

    # For successful API call, response code will be 200 (OK)
    if(getAllResponse.ok):

        # Loading the response data into a dict variable
        # json.loads takes in only binary or string variables so using content to fetch binary content
        # Loads (Load String) takes a Json file and converts into python data structure (dict or list, depending on JSON)
        jData = json.loads(getAllResponse.content)
    
        print("The response contains {0} properties".format(len(jData)))
        print("\n")
        for book in jData:
            #if len(book[u'author']) < 10:
            randomName = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(8))
            book[u'author'] = book[u'author'][:7] + randomName;
            #
            # update book
            putResponse = requests.put(url=url + str(book['id']), data = json.dumps(book), headers=headers) 
            #print putResponse
            #print putResponse 
            
    else:
        # If response code is not ok (200), print the resulting http error code with description
        getAllResponse.raise_for_status()
    logging.info('Exiting')

threads = []
for i in range(20):
    t = threading.Thread(target=updateAll)
    threads.append(t)
    t.start()

