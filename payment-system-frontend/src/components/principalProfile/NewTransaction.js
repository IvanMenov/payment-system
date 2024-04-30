import { fetchAPI, parseReadableStreamToJson } from '../../APIUtils';
import { API_BASE_URL } from '../../constants';
import { useState } from 'react';
import Box from '@mui/material/Box';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import FormControl from '@mui/material/FormControl';
import Select from '@mui/material/Select';
import TextField from '@mui/material/TextField';
import { Button } from 'react-bootstrap';
import { trackPromise } from 'react-promise-tracker';
import { Store } from 'react-notifications-component';

const NewTransaction = (props) => {

    const [user, setUser] = useState(props.currentUser)
    const [type, setType] = useState('CHARGE');
    const [customer, setCustomer] = useState({
        customerEmail: '',
        customerPhone: '',
        customerAmount: ''
    });

    const [transaction, setTransaction] = useState({
        amount: '',
        referenceId: ''
    })

    const handleTransactionTypeChange = (event) => {
        setType(event.target.value);
    };


    const handleChange = (event) => {
        let name = event.target.id;
        let value = event.target.value;
        if (name === 'amount' || name == 'referenceId') {
            setTransaction(transactions => ({ ...transactions, [name]: value }))
        }

        else {
            setCustomer(customers => ({ ...customers, [name]: value }))
        }
    };


    const validInput = () => {
        if (type == 'CHARGE') {
            if (transaction.amount == '' || customer.customerEmail == '' || customer.customerAmount == '' || customer.customerPhone == '') {
                return false;
            }
            return true;
        }
        else if (type == 'REFUND') {
            if (transaction.amount == '' || transaction.referenceId == '') {
                return false;
            }
            return true;
        } else {
            if (transaction.referenceId == '') {
                return false;
            }
            return true;
        }
    }

    const handleSubmit = (event) => {
        event.preventDefault();
        if (!validInput()) {
            Store.addNotification({
                message: "Invalid or missing input!",
                type: "danger",
                insert: "top",
                container: "top-right",
                animationIn: ["animate__animated", "animate__fadeIn"],
                animationOut: ["animate__animated", "animate__fadeOut"],
                dismiss: {
                    duration: 3000,
                    onScreen: true
                }
            });
        } else {
            const payment = {
                "amount": transaction.amount,
                "transactionType": type.toUpperCase(),
                "referenceId": transaction.referenceId,
                "customer": {
                    "customerEmail": customer.customerEmail,
                    "customerPhone": customer.customerPhone,
                    "customerAmount": customer.customerAmount
                }
            }

            trackPromise(
                fetchAPI("POST", API_BASE_URL + "/api/v1/payment/transactions/init", payment)
                    .then(response => {
                        Store.addNotification({
                            message: "Successfully initialized transaction!",
                            type: "success",
                            insert: "top",
                            container: "top-right",
                            animationIn: ["animate__animated", "animate__fadeIn"],
                            animationOut: ["animate__animated", "animate__fadeOut"],
                            dismiss: {
                                duration: 3000,
                                onScreen: true
                            }
                        });

                    }).catch(async error => {
                        const errs = await parseReadableStreamToJson(error.body);
                        Store.addNotification({
                            message: errs,
                            type: "danger",
                            insert: "top",
                            container: "top-right",
                            animationIn: ["animate__animated", "animate__fadeIn"],
                            animationOut: ["animate__animated", "animate__fadeOut"],
                            dismiss: {
                                duration: 5000,
                                onScreen: true
                            }
                        });
                    }))
        }



    }

    return (<div className="login-content">
        <h1 className="login-title">Create Transaction</h1>

        <Box sx={{ minWidth: 120 }}>

            <FormControl id="form_id" fullWidth margin='normal' >


                <InputLabel id="demo-simple-select-label">Transaction type</InputLabel>
                <Select
                    labelId="demo-simple-select-label"
                    id="transactionType"
                    value={type}
                    label="Transaction type"
                    onChange={handleTransactionTypeChange}
                >
                    <MenuItem value='CHARGE'>CHARGE</MenuItem>
                    <MenuItem value='REFUND'>REFUND</MenuItem>
                    <MenuItem value='REVERSAL'>REVERSAL</MenuItem>
                </Select>
                {
                    type == 'CHARGE' || type == 'REFUND' ?
                        <>
                            <TextField type="number" onChange={handleChange} id="amount" label="Amount" variant="outlined" margin="normal" value={transaction.amount || ""} />

                        </>
                        :
                        <>
                        </>
                }
                {type == 'CHARGE' ?
                    <>
                        <TextField type="email" onChange={handleChange} id="customerEmail" label="Customer email" variant="outlined" margin="normal" value={customer.customerEmail || ""} />
                        <TextField onChange={handleChange} id="customerPhone" label="Customer phone" variant="outlined" margin="normal" value={customer.customerPhone || ""} />
                        <TextField type="number" onChange={handleChange} id="customerAmount" label="Customer amount" variant="outlined" margin="normal" value={customer.customerAmount || ""} />

                    </>
                    :
                    <>

                    </>

                }
                {
                    type == "REFUND" || type == "REVERSAL" ?
                        <>
                            <TextField onChange={handleChange} id="referenceId" label="Reference Transaction uuid" variant="outlined" margin="normal" value={transaction.referenceId || ""} />

                        </>
                        :
                        <></>
                }

                <Button onClick={handleSubmit} type='submit' color="primary" size='lg' target='form_id'>Submit</Button>

            </FormControl>

        </Box>

    </div>)
}
export default NewTransaction