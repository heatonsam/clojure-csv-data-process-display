# Clojure(Script) CSV/DynamoDB Data Processing and Display Example

This project was written as a final-year project in college in April 2020 as an opportunity to learn Clojure(Script).

On the server side it can take a .csv file, read it into Clojure data, and persist it to an AWS DynamoDB database. It also provides API endpoints to access and manipulate this data.

On the client side it will access the data from DynamoDB via the API, display it in a sortable, filterable table, and display a graph about that data. Rows can also be added, removed, and modified this way.

As written, the code will work for the specific included CSV file, and there are some parts of the code where the column names from this file are specified. This was due to the assignment requirements, and the code can be easily adapted to take any CSV file and deal with the column names dynamically.

The DynamoDB credentials are not included constants.clj and therefore not all features will work without providing your own credentials.

This project takes advantage of the following Clojure(Script) or JavaScript libraries:

- Ring
- Compojure
- http-kit
- Faraday
- Reagent
- cljs-http
- React-vis
