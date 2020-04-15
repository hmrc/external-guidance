
# External Guidance backend

External Guidance backend shared between the manage-external-guidance-frontend and view-external-guidance-frontend microservices

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

## Endpoints

### `POST: /external-guidance/scratch`

#### Success Response

**HTTP Status**: `201`

**Example Response Body**:
```
{
   "id": "265e0178-cbe1-42ab-8418-7120ce6d0925"
}
```

#### Error Responses

**Error response format**
```
{
   "code": "ERROR_CODE",
   "message": "Human readable error message"
}
```

<table>
    <thead>
        <tr>
            <th>HTTP Status</th>
            <th>Error Code</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><pre>400</pre></td>
            <td><pre>BAD_REQUEST</pre></td>
        </tr>
        <tr>
            <td><pre>500</pre></td>
            <td><pre>INTERNAL_SERVER_ERROR</pre></td>
        </tr>
    </tbody>
</table>

### `GET: /external-guidance/scratch/:id`

#### `id` Parameter

The `id` parameter is an`UUID` formatted string e.g. `bf8bf6bb-0894-4df6-8209-2467bc9af6ae`

#### Success Response

**HTTP Status**: `200`

**Response Body**

Represents a valid guidance flow in JSON format.

#### Error Responses

**Error response format**
```
{
   "code": "ERROR_CODE",
   "message": "Human readable error message"
}
```

<table>
    <thead>
        <tr>
            <th>HTTP Status</th>
            <th>Error Code</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><pre>400</pre></td>
            <td><pre>BAD_REQUEST</pre></td>
        </tr>
        <tr>
            <td><pre>404</pre></td>
            <td><pre>NOT_FOUND_ERROR</pre></td>
        </tr>
        <tr>
            <td><pre>500</pre></td>
            <td><pre>INTERNAL_SERVER_ERROR</pre></td>
        </tr>
    </tbody>
</table>

### `POST: /external-guidance/submitted`

#### Success Response

**HTTP Status**: `201`

**Example Response Body**:
```
{
   "id": "oct90005"
}
```

#### Error Responses

**Error response format**
```
{
   "code": "ERROR_CODE",
   "message": "Human readable error message"
}
```

<table>
    <thead>
        <tr>
            <th>HTTP Status</th>
            <th>Error Code</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><pre>400</pre></td>
            <td><pre>BAD_REQUEST</pre></td>
        </tr>
        <tr>
            <td><pre>500</pre></td>
            <td><pre>INTERNAL_SERVER_ERROR</pre></td>
        </tr>
    </tbody>
</table>

### `GET: /external-guidance/submitted/:id`

#### `id` Parameter

The `id` parameter is an ocelot formatted string e.g. `oct90005`

#### Success Response

**HTTP Status**: `200`

**Response Body**

Represents a valid guidance flow in JSON format.

#### Error Responses

**Error response format**
```
{
   "code": "ERROR_CODE",
   "message": "Human readable error message"
}
```

<table>
    <thead>
        <tr>
            <th>HTTP Status</th>
            <th>Error Code</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><pre>400</pre></td>
            <td><pre>BAD_REQUEST</pre></td>
        </tr>
        <tr>
            <td><pre>404</pre></td>
            <td><pre>NOT_FOUND_ERROR</pre></td>
        </tr>
        <tr>
            <td><pre>500</pre></td>
            <td><pre>INTERNAL_SERVER_ERROR</pre></td>
        </tr>
    </tbody>
</table>
