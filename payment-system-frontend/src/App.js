import React, { Component } from 'react';
import { Route, Switch} from 'react-router-dom';
import Login from './components/login/Login';
import Profile from './components/userProfile/Profile';
import NotFound from './common/NotFound';
import { getCurrentUser } from './APIUtils';
import { ACCESS_TOKEN } from './constants';
import PrivateRoute from './common/PrivateRoute';
import { ReactNotifications } from 'react-notifications-component'
import 'react-notifications-component/dist/theme.css'
import './App.css';
import { Store } from 'react-notifications-component';
import ListMerchantTransactions from './components/userProfile/ListMerchantTransactions';


class App extends Component {

  notificationSystem = React.createRef();

  constructor(props) {
    super(props);
    this.state = {
      authenticated: false,
      currentUser: null,
    }

    this.loadCurrentlyLoggedInUser = this.loadCurrentlyLoggedInUser.bind(this);

  }

   loadCurrentlyLoggedInUser() {
    getCurrentUser().then(response =>
      {
        this.setState({
          currentUser:JSON.stringify(response),
          authenticated: true,
        });
      }).catch(error => {});    
}


  componentDidMount() {
    this.loadCurrentlyLoggedInUser();
  }

  render() {
    return (
      <div className="app">
        <ReactNotifications />
        <div className="app-body">
        
           <Switch>
           <Route exact path="/"
              render={(props) => <Login authenticated={this.state.authenticated} {...props} />}></Route>
             <Route path="/login"
              render={(props) => <Login authenticated={this.state.authenticated} {...props} />}></Route>
              <PrivateRoute path="/profile" authenticated={this.state.authenticated} currentUser={this.state.currentUser}
              component={Profile} ></PrivateRoute>

            <Route component={NotFound}></Route>
           
           </Switch>
          
        </div>
      </div>
    );
  }
}

export default App;
