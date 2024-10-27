import React from 'react';
import { Route, Switch } from 'react-router-dom';
import Profile from './components/principalProfile/Profile';
import NotFound from './common/NotFound';
import PrivateRoute from './common/PrivateRoute';
import { ReactNotifications } from 'react-notifications-component'
import 'react-notifications-component/dist/theme.css'
import './App.css';
import { useEffect, useState, Routes } from 'react';
import Keycloak from 'keycloak-js';
import { useHistory } from "react-router-dom"
import { systemLogin } from './APIUtils';


const App = () => {
  const history = useHistory();
  const notificationSystem = React.createRef();
  const [keycloakInitialized, setKeycloakInitialized] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);
  const [keycloakInstance, setKeycloakInstance] = useState({})
  const [currentPrincipal, setCurrentPrincipal] = useState({});


  useEffect(() => {
    // Initialize Keycloak and check if authenticated
    const keycloak = new Keycloak({
      url: `http://localhost:8081`,
      realm: "payment-system-realm",
      clientId: "payment-system-client"
    })
    keycloak.init({
      onLoad: 'login-required',
      pkceMethod: 'S256',
      redirectUri: window.location.origin
    })
      .then(auth => {
        console.log("Innitialized successfully")
        setKeycloakInitialized(true);
        setAuthenticated(auth);
        setKeycloakInstance(keycloak);
        if (auth) {
          console.log(" Keycloak token: " + keycloak.token)
          systemLogin(keycloak.token)
            .then(response => {
              console.log(response)
              setCurrentPrincipal(response)
              history.push('/profile');
            })


        } else {
          keycloak.login();
        }

      })
      .catch(err => {
        console.error('Keycloak initialization failed', err);
        setAuthenticated(false);
      });

  }, []);
  return (
    <div className="app">
      <ReactNotifications />
      <div className="app-body">
        <Switch>
          <PrivateRoute path="/profile" component={Profile} isAuthenticated={authenticated} keycloakInstance={keycloakInstance} principal={currentPrincipal} />
        </Switch>
      </div>
    </div>
  );

}

export default App;
