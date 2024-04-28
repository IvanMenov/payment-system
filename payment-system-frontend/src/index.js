import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import { BrowserRouter as Router } from 'react-router-dom';
import { createBrowserHistory } from 'history';
import Loader from 'react-loader-spinner';
import { usePromiseTracker } from "react-promise-tracker";
import { render } from 'react-dom';

const LoadingIndicator = props => {
  const { promiseInProgress } = usePromiseTracker();

  return promiseInProgress && 

   <div
     style={{
        width: "100%",
        height: "100",
       display: "flex",
        justifyContent: "center",
        alignItems: "center"
      }}
    >
      <Loader type="ThreeDots" color="#2BAD60" height="100" width="100" />
    </div>
};

const history = createBrowserHistory();
render(
  <Router history={history}>
    <App />
    <LoadingIndicator/>
  </Router>,
  document.getElementById('root')
);

reportWebVitals();
