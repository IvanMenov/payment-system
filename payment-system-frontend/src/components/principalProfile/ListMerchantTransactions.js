import * as React from 'react';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import Paper from '@mui/material/Paper';
import { useState, useEffect } from 'react';
import { getMerchant, initTransaction, parseReadableStreamToJson } from '../../APIUtils';
import { Store } from 'react-notifications-component';
import { Button, TablePagination } from '@mui/material';
import { trackPromise } from 'react-promise-tracker';


const ListMerchantTransactions = (props) => {
    const [user, setUser] = useState(props.currentUser)
    const [rows, setRows] = useState([])
    const [comesFromAdmin] = useState(props.comesFromAdmin)
    const [keycloakInstance] = useState(props.keycloakInstance);
    const [rowsPerPage, setRowsPerPage] = useState(5);
    const [page, setPage] = useState(0);

    useEffect(() => {
        updateTransactions();
    }, []);

    // Handle the change in page
    const handleChangePage = (event, newPage) => {
        setPage(newPage);
        updateTransactions();
    };

    // Handle the change in rows per page
    const handleChangeRowsPerPage = (event) => {
        setRowsPerPage(parseInt(event.target.value, 10));
        setPage(0);
    };

    async function performRefund(row) {
        const refund = {
            "transactionType": "REFUND",
            "referenceId": row.uuid,
            "amount": row.amount,
            "customer": {
                "customerEmail": row.customerEmail,
                "customerPhone": row.customerPhone
            }
        }
        trackPromise(
            initTransaction(refund, keycloakInstance.token, user.id)
                .then(response => {
                    updateTransactions();
                    setPage(0);

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
    async function performReversal(row) {
        const reversal = {
            "transactionType": "REVERSAL",
            "referenceId": row.uuid,
            "customer": {
                "customerEmail": row.customerEmail,
                "customerPhone": row.customerPhone
            }
        }
        trackPromise(
            initTransaction(reversal, keycloakInstance.token, user.id)
                .then(response => {
                    updateTransactions();
                    setPage(0);

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
    async function updateTransactions() {
        try {
            const updatePrincipal = await getMerchant(user.id, keycloakInstance.token, rowsPerPage, page)
            if (updatePrincipal != null && updatePrincipal.transactionList != null) {
                let list = updatePrincipal.transactionList
                setRows(updatePrincipal.transactionList)
                setUser(updatePrincipal)
                Store.addNotification({
                    message: "Successfully loaded transaction!",
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
            }

        } catch (error) {
            Store.addNotification({
                message: error,
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
        };

    }

    function ReversalOrRefundButton(props) {
        console.log()
        let row = props.rowData
        if (row.transactionType == "AUTHORIZE" && row.transactionStatus == "APPROVED") {
            return <Box
                my={2}
                display="flex"
                alignItems="center"
                gap={2}
                p={2}>
                <Button variant="contained" onClick={() => performReversal(row)}>
                    REVERSE
                </Button>
            </Box>
        } else if (row.transactionType == "CHARGE" && row.transactionStatus == "APPROVED") {
            return <Box
                my={2}
                display="flex"
                alignItems="center"
                gap={2}
                p={2}>
                <Button variant="contained" onClick={() => performRefund(row)}>
                    REFUND
                </Button>
            </Box>
        } else {
            return <></>
        }

    }
    return (
        <Box
            component="form"
            sx={{ '& .MuiTextField-root': { m: 1, width: '25ch' }, }}
            noValidate
            autoComplete="off">
            <Box
                gap={2}
                p={2}>
                {
                    comesFromAdmin === true ?
                        <>
                            <Button sx={{ marginTop: '10px' }}
                                variant="contained" type='submit' >
                                Back
                            </Button>
                        </>
                        : <></>

                }

                <TextField
                    value={user.email}
                    id="outlined-read-only-input"
                    label="Merchant email"
                    InputProps={{
                        min: 0,
                        readOnly: true,
                        style: { textAlign: 'center' }
                    }}

                />
                <TextField
                    value={user.totalTransactionSum == null ? 0 : user.totalTransactionSum}
                    id="outlined-read-only-input"
                    label="Total Transaction Sum"
                    InputProps={{
                        min: 0,
                        readOnly: true,
                        style: { textAlign: 'center' }
                    }}

                />
            </Box>
            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell sx={{ border: 2, textAlign: 'center' }}>Transaction UUID</TableCell>
                            <TableCell sx={{ border: 2, textAlign: 'center' }} >Amount</TableCell>
                            <TableCell sx={{ border: 2, textAlign: 'center' }}>Customer Email</TableCell>
                            <TableCell sx={{ border: 2, textAlign: 'center' }}>customer Phone</TableCell>
                            <TableCell sx={{ border: 2, textAlign: 'center' }}>Reference UUID</TableCell>
                            <TableCell sx={{ border: 2, textAlign: 'center' }}>Status</TableCell>
                            <TableCell sx={{ border: 2, textAlign: 'center' }}>Type</TableCell>
                            <TableCell sx={{ border: 2, textAlign: 'center' }}>Date/Time</TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {rows.map((row) => (
                            <TableRow>
                                <TableCell>{row.uuid}</TableCell>
                                <TableCell>{row.amount}</TableCell>
                                <TableCell>{row.customerEmail == null ? 'Not available' : row.customerEmail}</TableCell>
                                <TableCell>{row.customerPhone == null ? 'Not available' : row.customerPhone}</TableCell>
                                <TableCell>{row.referenceTransactionUUID == null ? 'Not available' : row.referenceTransactionUUID}</TableCell>
                                <TableCell>{row.transactionStatus == null ? 'CREATED' : row.transactionStatus}</TableCell>
                                <TableCell>{row.transactionType}</TableCell>
                                <TableCell>
                                    {
                                        new Intl.DateTimeFormat('en-US',
                                            { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' })
                                            .format(row.timestamp)

                                    }
                                </TableCell>
                                {comesFromAdmin !== true ?
                                    <>
                                        <ReversalOrRefundButton rowData={row}></ReversalOrRefundButton>
                                    </>

                                    :
                                    <></>
                                }
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            </TableContainer>
            <Box
                display="flex"
                justifyContent="center"
                alignItems="center"
                
            >
                <TablePagination
                    component="div"
                    count={user.countTransactions}
                    page={page}
                    onPageChange={handleChangePage}
                    rowsPerPage={rowsPerPage}
                    onRowsPerPageChange={handleChangeRowsPerPage}
                    rowsPerPageOptions={[5, 10, 25]}

                />
            </Box>

        </Box>

    );
}

export default ListMerchantTransactions;