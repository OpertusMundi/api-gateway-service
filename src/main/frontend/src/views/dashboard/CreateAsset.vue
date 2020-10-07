<template>
<div class="dashboard__inner">
    <div class="dashboard__inner__steps" v-if="!uploading.status">
      <div class="dashboard__head">
        <h1>Add an asset</h1>
        <div class="dashboard__head__helpers">
          <a href="#" class="btn btn--outlineblue">SAVE DRAFT</a>
          <a href="#" class="btn btn--outlineblue">EXIT</a>
        </div>
      </div>

      <div class="dashboard__form">
        <ul class="dashboard__form__nav">
          <li><a href="#" :class="[currentStep == 1 ? 'active' : '', currentStep < 1 ? 'inactive' : '']" @click="goToStep(1)">Asset Type</a></li>
          <li><a href="#" :class="[currentStep == 2 ? 'active' : '', currentStep < 2 ? 'inactive' : '']" @click="goToStep(2)">Metadata</a></li>
          <li><a href="#" :class="[currentStep == 3 ? 'active' : '', currentStep < 3 ? 'inactive' : '']" @click="goToStep(3)">Contract</a></li>
          <li><a href="#" :class="[currentStep == 4 ? 'active' : '', currentStep < 4 ? 'inactive' : '']" @click="goToStep(4)">Pricing</a></li>
          <li><a href="#" :class="[currentStep == 5 ? 'active' : '', currentStep < 5 ? 'inactive' : '']" @click="goToStep(5)">Delivery</a></li>
          <li><a href="#" :class="[currentStep == 6 ? 'active' : '', currentStep < 6 ? 'inactive' : '']" @click="goToStep(6)">Payout</a></li>
          <li><a href="#" :class="[currentStep == 7 ? 'active' : '', currentStep < 7 ? 'inactive' : '']" @click="goToStep(7)">Review</a></li>
        </ul>
        <div class="dashboard__form__steps">

          <validation-observer ref="step1">
          <div class="dashboard__form__step" v-if="currentStep == 1">
              <div class="dashboard__form__step__title">
                <h3>Select asset type</h3>
                <p>Select the type of your asset. <br>Note that the next steps will be optimised based on this type.</p>
              </div>
              <validation-provider v-slot="{ errors }" name="Asset Type" rules="required">
                <div class="form-group">
                  <label class="control control-radio">
                    Data File
                    <input type="radio" name="asset_type" v-model="asset.type" value="datafile" />
                    <div class="control_indicator"></div>
                  </label>
                  <label class="control control-radio">
                    API
                    <input type="radio" name="asset_type" v-model="asset.type" value="API" />
                    <div class="control_indicator"></div>
                  </label>
                  <label class="control control-radio">
                    Collection
                    <input type="radio" name="asset_type" v-model="asset.type" value="collection" />
                    <div class="control_indicator"></div>
                  </label>
                  <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span> </div>
                </div>
              </validation-provider>
          </div>
          </validation-observer>

          <validation-observer ref="step2">
          <div class="dashboard__form__step" v-if="currentStep == 2">
            <div class="dashboard__form__step__title">
              <h3>Add asset metadata</h3>
              <p>Fill in your asset metadata. If you already have compatible metadata, please upload them. You can still edit them. <br>All metadata will be available to prospective clients under a CC-BY-NC 4.0 license to facilitate asset discovery from consumers.</p>
            </div>
            <validation-provider v-slot="{ errors }" name="Language" rules="required">
              <div class="form-group">
                <label for="metadata_language">Language</label>
                <input type="text" class="form-group__text" name="metadata_language" id="metadata_language" v-model="asset.language">
                <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
              </div>
            </validation-provider>
            <validation-provider v-slot="{ errors }" name="Editor's name" rules="required">
              <div class="form-group">
                <label for="editor">Editor</label>
                <input type="text" name="publisherName" class="form-group__text" id="" v-model="asset.publisherName">
                <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
              </div>
            </validation-provider>
            <validation-provider v-slot="{ errors }" name="Editor's email" rules="required|email">
              <div class="form-group">
                <label for="">Editor’s email</label>
                <input type="text" class="form-group__text" id="" name="publisherEmail" v-model="asset.publisherEmail">
                <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
              </div>
            </validation-provider>
            <validation-provider v-slot="{ errors }" name="Maintenance manager name" rules="required">
              <div class="form-group">
                <label for="">Maintenance manager</label>
                <input type="text" class="form-group__text" id="" name="metadataPointOfContactName" v-model="asset.metadataPointOfContactName">
                <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
              </div>
            </validation-provider>
            <validation-provider v-slot="{ errors }" name="Maintenance manager’s email" rules="required|email">
              <div class="form-group">
                <label for="">Maintenance manager’s email</label>
                <input type="text" class="form-group__text" id="" name="metadataPointOfContactEmail" v-model="asset.metadataPointOfContactEmail">
                <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
              </div>
            </validation-provider>
            <validation-provider v-slot="{ errors }" name="Version" rules="required">
              <div class="form-group">
                <label for="">Version</label>
                <input type="text" class="form-group__text" id="" name="version" v-model="asset.version">
                <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
              </div>
            </validation-provider>
            <validation-provider v-slot="{ errors }" name="Asset title" rules="required">
              <div class="form-group">
                <label for="">Asset title</label>
                <input type="text" class="form-group__text" id="" name="title" v-model="asset.title">
                <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
              </div>
            </validation-provider>
            <validation-provider v-slot="{ errors }" name="Asset short description" rules="required">
              <div class="form-group">
                <label for="">Asset short description</label>
                <input type="text" class="form-group__text" id="" name="abstractText" v-model="asset.abstractText">
                <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
              </div>
            </validation-provider>
            <validation-provider v-slot="{ errors }" name="Metadata language" rules="required">
              <div class="form-group">
                <label for="">Metadata language</label>
                <input type="text" class="form-group__text" name="metadataLanguage" id="" v-model="asset.metadataLanguage">
                <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
              </div>
            </validation-provider>
            <validation-provider v-slot="{ errors }" name="Metadata date" rules="required">
              <div class="form-group">
                <label for="">Metadata date</label>
                <datepicker v-model="asset.metadataDate" name="metadataDate" format="dd/MM/yyyy" input-class="form-group__text"></datepicker>
                <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
              </div>
            </validation-provider>
            <validation-provider v-slot="{ errors }" name="Scale" rules="required">
              <div class="form-group">
                <label for="">Scale</label>
                <input type="text" class="form-group__text" id="" name="scale" v-model="asset.scale">
                <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
              </div>
            </validation-provider>

          </div>
          </validation-observer>

          <validation-observer ref="step3">
          <div class="dashboard__form__step" v-if="currentStep == 3">
            <div class="dashboard__form__step__title">
              <h3>Contract template</h3>
              <p>Assign a contract template to your asset</p>
            </div>
            <div class="dashboard__form__step__contract">
              <div class="dashboard__form__step__contract__inner">
                <h5>Existing templates</h5>
                <p>Assign a contract template to your asset</p>
                  <validation-provider v-slot="{ errors }" name="Contract Tamplate" rules="required">
                    <div class="form-group">
                      <multiselect v-model="contract" :options="['Contract template #1','Contract template #2','Contract template #3']" :searchable="false" :close-on-select="true" :show-labels="false" placeholder="Select a contract template"></multiselect>
                      <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span> </div>
                    </div>
                  </validation-provider>
              </div>
              <div class="dashboard__form__step__contract__seperator">or</div>
              <div class="dashboard__form__step__contract__inner">
                <h5>Create a new one</h5>
                <p>By creating a new contract, you will exit this wizard, the asset will be saved as draft and you can continue editing it afterwards.</p>
                <a href="#">Create a new contract</a>
              </div>
            </div>
          </div>
          </validation-observer>

          <validation-observer ref="step4">
          <div class="dashboard__form__step" v-if="currentStep == 4">
            <div class="dashboard__form__step__title">
              <h3>Pricing model</h3>
              <p>Assign one or more pricing models to your asset. If there are multiple, select one as default. Some options may be disabled based on the previous steps. All prices must not include VAT.</p>
            </div>
            <validation-provider v-slot="{ errors }" name="Asset Type" rules="required">
              <div class="form-group">
                <label class="control control-radio">
                  Fixed price <span>Add the price of your asset in euros and choose from the other provided options. </span>
                  <input type="radio" name="asset_type" v-model="priceModelType" value="FIXED" @change="setPriceModel()" />
                  <div class="control_indicator"></div>
                </label>
                <label class="control control-radio">
                  Subscription <span>Subscriptions to services are provided for a specific time period and are automatically renewed. </span>
                  <input type="radio" name="asset_type" v-model="priceModelType" value="SUBSCRIPTION" @change="setPriceModel()" />
                  <div class="control_indicator"></div>
                </label>
                <label class="control control-radio">
                  Free <span>Provide your asset for free.</span>
                  <input type="radio" name="asset_type" v-model="priceModelType" value="FREE" @change="setPriceModel()" />
                  <div class="control_indicator"></div>
                </label>
                <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span> </div>
              </div>
            </validation-provider>
            <div class="dashboard__form__step__pricing-model" v-if="priceModelType && priceModelType !== 'FREE'">
              <div class="asset-pricing-model" v-for="(pricingModel,index) in asset.pricingModels" v-bind:key="index+'pr_model'">
                <div class="asset-pricing-model__fixed" v-if="priceModelType == 'FIXED'">
                  <validation-provider v-slot="{ errors }" name="Price" rules="required"  tag="div" class="form-group form-group--inline">
                      <label for="">Price in EUR</label>
                      <input type="number" class="form-group__text" value="" v-model="pricingModel.totalPriceExcludingTax">
                    <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span> </div>
                  </validation-provider>
                  <validation-provider v-slot="{ errors }" name="Updates" rules="required" tag="div" class="form-group form-group--inline">
                    <label for="">Updates</label>
                    <multiselect v-model="pricingModel.yearsOfUpdates" :options="['0','1','2','3','4','5','6','7','8','9','10','11','12']" :searchable="false" :close-on-select="true" :show-labels="false" placeholder="">
                      <template slot="singleLabel" slot-scope="props">{{ props.option }} years</template>
                      <template slot="option" slot-scope="props">{{ props.option }} years</template>
                    </multiselect>
                    <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span> </div>
                  </validation-provider>
                  <div class="form-group form-group--inline" style="min-width: unset;" v-if="index >= 1">
                    <a href="#" @click.prevent="removePricingModel(index)" class="dashboard__form__step__pricing-model__remove">- REMOVE</a>
                  </div>
                </div>
                <div class="asset-pricing-model__subscription" v-if="priceModelType == 'SUBSCRIPTION'">
                  <validation-provider v-slot="{ errors }" name="Price" rules="required"  tag="div" class="form-group form-group--inline">
                      <label for="">Monthly Price in EUR</label>
                      <input type="number" class="form-group__text" value="" v-model="pricingModel.monthlyPrice">
                    <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span> </div>
                  </validation-provider>
                  <validation-provider v-slot="{ errors }" name="Updates" rules="required" tag="div" class="form-group form-group--inline">
                    <label for="">Time Period</label>
                    <multiselect v-model="pricingModel.duration" :options="['0','1','2','3','4','5','6','7','8','9','10','11','12']" :searchable="false" :close-on-select="true" :show-labels="false" placeholder="">
                      <template slot="singleLabel" slot-scope="props">{{ props.option }} months</template>
                      <template slot="option" slot-scope="props">{{ props.option }} months</template>
                    </multiselect>
                    <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span> </div>
                  </validation-provider>
                  <div class="form-group form-group--inline" style="min-width: unset;" v-if="index >= 1">
                    <a href="#" @click.prevent="removePricingModel(index)" class="dashboard__form__step__pricing-model__remove">- REMOVE</a>
                  </div>
                </div>
              </div>
              <a href="#" @click.prevent="addPricingModel" class="dashboard__form__step__pricing-model__add">+ Add pricing model</a>
            </div>
          </div>
          </validation-observer>

          <validation-observer ref="step5">
          <div class="dashboard__form__step dashboard__form__step--delivery" v-if="currentStep == 5">
            <div class="dashboard__form__step__delivery">
              <div class="dashboard__form__step__delivery__inner">
                <div class="dashboard__form__step__title">
                  <h3>Asset delivery</h3>
                  <p>Select how your asset will reach consumers.</p>
                </div>
                <validation-provider v-slot="{ errors }" name="Delivery method" rules="required">
                <div class="form-group">
                  <label class="control control-radio">
                    By the platform
                    <span>You can upload your data asset securely in the platform. Customers will be able to download the data asset only after a transaction is made.</span>
                    <input type="radio" name="asset_delivery" v-model="asset.deliveryType" value="platform" />
                    <div class="control_indicator"></div>
                  </label>
                  <label class="control control-radio">
                    By own means
                    <span>If your data is too large, updated daily, or you do not want to use our repository services, you can handle the delivery of the data asset to consumer through your own channel.</span>
                    <input type="radio" name="asset_delivery" v-model="asset.deliveryType" value="owm_means" />
                    <div class="control_indicator"></div>
                  </label>
                  <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span> </div>
                </div>
                </validation-provider>
              </div>
              <div class="dashboard__form__step__delivery__inner" v-if="asset.deliveryType === 'platform'">
                <div class="dashboard__form__step__title">
                  <p>Select specifically how the platform will have access to your asset</p>
                </div>
                <validation-provider v-slot="{ errors }" name="Asset access" rules="required">
                <div class="form-group">
                  <label class="control control-radio">
                    Direct upload
                    <input type="radio" name="asset_access" v-model="asset.assetAccess" value="upload" />
                    <div class="control_indicator"></div>
                  </label>
                  <label class="control control-radio">
                    Link to asset
                    <input type="radio" name="asset_access" v-model="asset.assetAccess" value="link" />
                    <div class="control_indicator"></div>
                  </label>
                  <label class="control control-radio">
                    From Web Service
                    <input type="radio" name="asset_access" v-model="asset.assetAccess" value="webservice" />
                    <div class="control_indicator"></div>
                  </label>
                  <label class="control control-radio">
                    Select from Storage
                    <input type="radio" name="asset_access" v-model="asset.assetAccess" value="storage" />
                    <div class="control_indicator"></div>
                  </label>
                  <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span> </div>
                </div>
                </validation-provider>
                <div class="dashboard__form__step__delivery__access dashboard__form__step__delivery__access--upload" v-if="asset.assetAccess === 'upload'"></div>
                <div class="dashboard__form__step__delivery__access dashboard__form__step__delivery__access--link" v-if="asset.assetAccess === 'link'">
                  <validation-provider v-slot="{ errors }" name="Link" :rules="{ required: true, regex: /^(http:\/\/www\.|https:\/\/www\.|http:\/\/|https:\/\/)?[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/ }">
                  <div class="form-group">
                    <label for="asset_link">Please provide a direct web accessible link for the system to download the file from. </label>
                    <input type="text" class="form-group__text" id="asset_link" v-model="asset.link">
                    <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
                  </div>
                  </validation-provider>
                </div>
                <div class="dashboard__form__step__delivery__access dashboard__form__step__delivery__access--webservice" v-if="asset.assetAccess === 'webservice'"></div>
                <div class="dashboard__form__step__delivery__access dashboard__form__step__delivery__access--storage" v-if="asset.assetAccess === 'storage'"></div>
              </div>
              <div class="dashboard__form__step__delivery__inner" v-if="asset.deliveryType === 'owm_means'">
                <div class="dashboard__form__step__title">
                  <p>Please select one of the following options and provide the requested information. Delivery must take place within three (3) working days following a proof of payment provided to us by the consumer.</p>
                </div>
                <validation-provider v-slot="{ errors }" name="Own Means" rules="required">
                <div class="form-group">
                  <label class="control control-radio">
                    Digital delivery
                    <input type="radio" name="asset_access" v-model="asset.ownMeansType" value="digital" />
                    <div class="control_indicator"></div>
                  </label>
                  <label class="control control-radio">
                    Physical media
                    <input type="radio" name="asset_access" v-model="asset.ownMeansType" value="physical" />
                    <div class="control_indicator"></div>
                  </label>
                  <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span> </div>
                </div>
                </validation-provider>
                <div class="dashboard__form__step__delivery__access dashboard__form__step__delivery__access--digital" v-if="asset.ownMeansType === 'digital'">
                  <validation-provider v-slot="{ errors }" name="Notes for buyer" rules="required">
                  <div class="form-group">
                    <label for="asset_link">Notes for buyer</label>
                    <textarea name="" id="" cols="20" rows="5" v-model="asset.ownMeansDigitalNotes"></textarea>
                    <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
                  </div>
                  </validation-provider>
                </div>
                <div class="dashboard__form__step__delivery__access dashboard__form__step__delivery__access--physical" v-if="asset.ownMeansType === 'physical'">
                  <validation-provider v-slot="{ errors }" name="Type of physical media" rules="required">
                    <div class="form-group">
                      <label for="">Type of physical media</label>
                      <input type="text" class="form-group__text" id="" v-model="asset.ownMeansPhysicallMediaType">
                      <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
                    </div>
                  </validation-provider>
                  <validation-provider v-slot="{ errors }" name="Number of objects" rules="required">
                    <div class="form-group">
                      <label for="">Number of objects</label>
                      <input type="text" class="form-group__text" id="" v-model="asset.ownMeansPhysicallMediaObjects">
                      <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
                    </div>
                  </validation-provider>
                  <validation-provider v-slot="{ errors }" name="Notes for buyer" rules="required">
                  <div class="form-group">
                    <label for="asset_link">Notes for buyer</label>
                    <textarea name="" id="" cols="20" rows="5" v-model="asset.ownMeansPhysicallNotes"></textarea>
                    <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
                  </div>
                  </validation-provider>
                </div>
              </div>
            </div>
          </div>
          </validation-observer>

          <validation-observer ref="step6">
          <div class="dashboard__form__step" v-if="currentStep == 6">
              <div class="dashboard__form__step__title">
                <h3>Payout method</h3>
                <p>Select where your profits will be transferred</p>
              </div>
              <validation-provider v-slot="{ errors }" name="Asset Type" rules="required">
                <div class="form-group">
                  <label class="control control-radio">
                    Through the platform
                    <input type="radio" name="asset_type" v-model="asset.paymentMethod" value="platform" />
                    <div class="control_indicator"></div>
                  </label>
                  <label class="control control-radio">
                    External means
                    <input type="radio" name="asset_type" v-model="asset.paymentMethod" value="external" />
                    <div class="control_indicator"></div>
                  </label>
                  <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span> </div>
                </div>
              </validation-provider>
              <div class="dashboard__form__step__payment" v-if="asset.paymentMethod === 'external'">
                <validation-provider v-slot="{ errors }" name="Contract Tamplate" rules="required">
                <div class="form-group">
                  <multiselect v-model="asset.paymentType" :options="['Paypal', 'Credit/Debit Card', 'Direct bank transfer']" :searchable="false" :close-on-select="true" :show-labels="false" placeholder="Select a payment method"></multiselect>
                  <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span> </div>
                </div>
                </validation-provider>
                <div class="dashboard__form__step__payment__form" v-if="asset.paymentType === 'Paypal'">
                  <div class="form-group-double">
                    <validation-provider v-slot="{ errors }" name="Holder first name" rules="required" tag="div" class="form-group">
                      <label for="paypalFirstName">Holder first name</label>
                      <input type="text" class="form-group__text" id="paypalFirstName" v-model="asset.paypalFirstName">
                      <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
                    </validation-provider>
                    <validation-provider v-slot="{ errors }" name="Holder last name" rules="required" tag="div" class="form-group">
                      <label for="paypalLastName">Holder last name</label>
                      <input type="text" class="form-group__text" id="paypalLastName" v-model="asset.paypalLastName">
                      <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
                    </validation-provider>
                  </div>
                  <validation-provider v-slot="{ errors }" name="Paypal account" rules="required" tag="div" class="form-group">
                    <label for="paypalAccount">Paypal account</label>
                    <input type="text" class="form-group__text" id="paypalAccount" v-model="asset.paypalAccount">
                    <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
                  </validation-provider>
                </div>
                <div class="dashboard__form__step__payment__form" v-if="asset.paymentType === 'Credit/Debit Card'">
                  <validation-provider v-slot="{ errors }" name="Holder name" rules="required" tag="div" class="form-group">
                    <label for="creditCardholder">Holder name</label>
                    <input type="text" class="form-group__text" id="creditCardholder" v-model="asset.creditCardHolder">
                    <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
                  </validation-provider>
                  <div class="form-group-multiple">
                    <validation-provider v-slot="{ errors }" name="Card number" mode="lazy" rules="required|credit_card" tag="div" class="form-group form-group--full">
                      <label for="creditCardNumber">Card number</label>
                      <input type="text" class="form-group__text form-group__text--card" id="creditCardNumber" v-model="asset.creditCardNumber" v-cardformat:formatCardNumber>
                      <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
                    </validation-provider>
                    <validation-provider v-slot="{ errors }" name="Exp. date" mode="lazy" rules="required|credit_card_exp" tag="div" class="form-group">
                      <label for="creditCardDate">Exp. date</label>
                      <input type="text" class="form-group__text form-group__text--expdate" id="creditCardDate|credit_card_cvc" v-model="asset.creditCardDate" v-cardformat:formatCardExpiry>
                      <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
                    </validation-provider>
                    <validation-provider v-slot="{ errors }" name="Card CVC" mode="lazy" rules="required" tag="div" class="form-group">
                      <label for="creditCardCvc">CVC</label>
                      <input type="text" class="form-group__text form-group__text--cvc" id="creditCardCvc" v-model="asset.creditCardCvc" v-cardformat:formatCardCVC>
                      <div class="errors" v-if="errors"><span v-for="error in errors" v-bind:key="error">{{ error }}</span></div>
                    </validation-provider>
                  </div>
                </div>
                <!-- <div class="dashboard__form__step__payment__form" v-if="asset.paymentType === 'Direct bank transfer'">direct</div> -->
              </div>
          </div>
          </validation-observer>

          <validation-observer ref="step7">
          <div class="dashboard__form__step dashboard__form__step--review" v-if="currentStep == 7">
            <div class="dashboard__form__review">
              <div class="dashboard__form__review__inner">

                <div class="dashboard__form__review__item">
                  <div class="dashboard__form__review__item__head">
                    <h5>Asset type</h5>
                    <a href="#" @click.prevent="goToStep(1)">EDIT</a>
                  </div>
                  <div class="dashboard__form__review__item__body">
                    <ul>
                      <li><strong>Type:</strong>{{ asset.type }}</li>
                    </ul>
                  </div>
                </div>

                <div class="dashboard__form__review__item">
                  <div class="dashboard__form__review__item__head">
                    <h5>Metadata</h5>
                    <a href="#" @click.prevent="goToStep(2)">EDIT</a>
                  </div>
                  <div class="dashboard__form__review__item__body">
                    <ul>
                      <li><strong>Language:</strong>{{ asset.language }}</li>
                      <li><strong>Editor:</strong>{{ asset.publisherName }}</li>
                      <li><strong>Editor’s email:</strong>{{ asset.publisherEmail }}</li>
                      <li><strong>Maintenance manager:</strong>{{ asset.metadataPointOfContactName }}</li>
                      <li><strong>Maintenance manager’s email:</strong>{{ asset.metadataPointOfContactEmail }}</li>
                      <li><strong>Version:</strong>{{ asset.version }}</li>
                      <!-- <li><strong>Identifier:</strong>{{ asset.language }}</li> -->
                      <li><strong>Asset title:</strong>{{ asset.title }}</li>
                      <li><strong>Asset short description:</strong>{{ asset.abstractText }}</li>
                      <li><strong>Metadata language:</strong>{{ asset.metadataLanguage }}</li>
                      <li><strong>Metadata date:</strong>{{ asset.metadataDate }}</li>
                      <li><strong>Scale:</strong>{{ asset.scale }}</li>
                    </ul>
                  </div>
                </div>
              </div>
              <div class="dashboard__form__review__inner">

                <div class="dashboard__form__review__item">
                  <div class="dashboard__form__review__item__head">
                    <h5>Contract</h5>
                    <a href="#" @click.prevent="goToStep(3)">EDIT</a>
                  </div>
                  <div class="dashboard__form__review__item__body">
                    <ul>
                      <li><strong>Template:</strong></li>
                    </ul>
                  </div>
                </div>

                <div class="dashboard__form__review__item">
                  <div class="dashboard__form__review__item__head">
                    <h5>Pricing</h5>
                    <a href="#" @click.prevent="goToStep(4)">EDIT</a>
                  </div>
                  <div class="dashboard__form__review__item__body">
                    <ul>
                      <li><strong>Pricing type:</strong>{{ priceModelType }}</li>
                      <li v-for="(pricingModel, index) in asset.pricingModels" v-bind:key="`pricingmodel${index}`">
                        <span v-if="priceModelType === 'FIXED'">
                          <strong>Pricing model {{ index+1 }}:</strong>{{ pricingModel.totalPrice }}€ + {{ pricingModel.yearsOfUpdates }} years
                        </span>
                        <span v-if="priceModelType === 'SUBSCRIPTION'">
                          <strong>Pricing model {{ index+1 }}:</strong>{{ pricingModel.monthlyPrice }}€ + {{ pricingModel.duration }} months
                        </span>
                        <span v-if="priceModelType === 'FREE'">
                          <strong>Pricing model {{ index+1 }}:</strong>FREE
                        </span>
                      </li>
                    </ul>
                  </div>
                </div>

                <div class="dashboard__form__review__item">
                  <div class="dashboard__form__review__item__head">
                    <h5>Delivery</h5>
                    <a href="#" @click.prevent="goToStep(5)">EDIT</a>
                  </div>
                  <div class="dashboard__form__review__item__body">
                    <ul>
                      <li><strong>Method:</strong>{{ asset.deliveryType }}</li>
                    </ul>
                  </div>
                </div>

                <div class="dashboard__form__review__item">
                  <div class="dashboard__form__review__item__head">
                    <h5>Payout</h5>
                    <a href="#" @click.prevent="goToStep(6)">EDIT</a>
                  </div>
                  <div class="dashboard__form__review__item__body">
                    <ul>
                      <li><strong>Method:</strong>{{ asset.paymentMethod }}</li>
                    </ul>
                  </div>
                </div>

              </div>
            </div>
          </div>
          </validation-observer>
          <div class="dashboard__form__errors" v-if="uploading.errors.length">
            <ul>
              <li v-for="error in uploading.errors" v-bind:key="`error${error.code}`">{{ error.description }}</li>
            </ul>
          </div>

        </div>
        <div class="dashboard__form__navbuttons">
          <button class="btn--std btn--blue" @click.prevent="previousStep()">PREVIOUS</button>
          <button class="btn--std btn--blue" @click.prevent="nextStep()">{{ currentStep === totalSteps ? 'confirm and submit for review' : 'NEXT' }}</button>
        </div>
      </div>
    </div>

    <div class="dashboard__form__uploading" v-if="uploading.status">
      <div class="dashboard__form__uploading__inner">
        <div class="dashboard__form__uploading__pbar">
          <span :style="{width: uploading.percentage + '%'}"></span>
          <p v-if="!uploading.completed">{{ uploading.percentage }}%</p>
          <p v-if="uploading.completed">Well done!</p>
        </div>
        <div class="dashboard__form__uploading__body">
          <h4>{{ uploading.title }}</h4>
          <p>{{ uploading.subtitle }}</p>
          <div class="dashboard__form__uploading__body__btns">
            <router-link to="/dashboard" class="btn btn--std btn--blue">GO TO Dashboard</router-link>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import { CatalogueAddItemCommand, ServerResponse } from '@/model';
import { BasePricingModelCommand } from '@/model/pricing-model';
import CatalogueApi from '@/service/catalogue';
import { required, email, regex } from 'vee-validate/dist/rules';
import { ValidationProvider, ValidationObserver, extend } from 'vee-validate';
import Multiselect from 'vue-multiselect';
import 'vue-multiselect/dist/vue-multiselect.min.css';
import VueCardFormat from 'vue-credit-card-validation';
import { AxiosError, AxiosRequestConfig } from 'axios';
import Datepicker from 'vuejs-datepicker';
import moment from 'moment';

Vue.use(VueCardFormat);

extend('required', required);
extend('email', email);
extend('regex', regex);

extend('credit_card', (value) => Vue.prototype.$cardFormat.validateCardNumber(value));
extend('credit_card_exp', (value) => Vue.prototype.$cardFormat.validateCardExpiry(value));
extend('credit_card_cvc', (value) => Vue.prototype.$cardFormat.validateCardCVC(value));

@Component({
  components: {
    ValidationProvider,
    ValidationObserver,
    Multiselect,
    Datepicker,
  },
})
export default class CreateAsset extends Vue {
  $refs!: {
    step1: InstanceType<typeof ValidationObserver>,
    step2: InstanceType<typeof ValidationObserver>,
    step3: InstanceType<typeof ValidationObserver>,
    step4: InstanceType<typeof ValidationObserver>,
    step5: InstanceType<typeof ValidationObserver>,
    step6: InstanceType<typeof ValidationObserver>,
    step7: InstanceType<typeof ValidationObserver>,
  }

  catalogueApi: CatalogueApi;

  asset: CatalogueAddItemCommand;

  priceModelType: string;

  totalSteps = 7;

  currentStep = 1;

  creatidCardValid = false;

  uploading:any;

  constructor() {
    super();

    this.asset = {
      abstractText: '',
      additionalResources: '',
      conformity: '',
      coupledResource: '',
      creationDate: '2020-06-02',
      dateEnd: '2020-06-02',
      dateStart: '2020-06-02',
      format: 'CSV',
      keywords: [],
      language: '',
      license: '',
      lineage: '',
      metadataDate: '2020-06-02',
      metadataLanguage: '',
      metadataPointOfContactEmail: '',
      metadataPointOfContactName: '',
      parentId: '',
      publicAccessLimitations: '',
      publicationDate: '2020-06-02',
      publisherEmail: '',
      publisherName: '',
      referenceSystem: '',
      resourceLocator: '',
      revisionDate: '2020-06-02',
      scale: '',
      spatialDataServiceType: '',
      spatialResolution: '',
      title: '',
      topicCategory: '',
      type: '',
      version: '',
      pricingModels: [],
      geometry: {
        type: 'Polygon',
        coordinates: [
          [
            [20.94818115234375, 36.40359962073253],
            [23.57940673828125, 36.40359962073253],
            [23.57940673828125, 38.31795595794451],
            [20.94818115234375, 38.31795595794451],
            [20.94818115234375, 36.40359962073253],
          ],
        ],
      } as GeoJSON.Polygon,
    };

    this.priceModelType = '';
    const priceModel = { type: '' } as BasePricingModelCommand;
    this.asset.pricingModels = [];
    this.asset.pricingModels.push(priceModel);

    this.uploading = {
      status: false,
      percentage: 0,
      title: 'Your asset is being uploaded',
      subtitle: 'Don’t close this page until upload is complete',
      completed: false,
      errors: [],
    };


    this.catalogueApi = new CatalogueApi();
  }

  // mounted():void {
  //   console.log(this.$route.params.error);
  //   console.log(this.asset.pricingModels);
  // }
  goToStep(step:number):void {
    this.currentStep = step;
  }

  previousStep():void {
    if (this.currentStep >= 1) {
      this.currentStep -= 1;
    }
  }

  nextStep():void {
    this.$refs[`step${this.currentStep}`].validate().then((success) => {
      if (success) {
        if (this.currentStep === this.totalSteps) {
          this.submitForm();
        } else {
          this.currentStep += 1;
          window.scrollTo({
            top: 0,
            behavior: 'smooth',
          });
        }
      }
    });
  }

  setPriceModel():void {
    const priceModel = { type: this.priceModelType } as BasePricingModelCommand;
    this.asset.pricingModels = [];
    this.asset.pricingModels.push(priceModel);
  }

  addPricingModel():void {
    const priceModel = {} as BasePricingModelCommand;
    this.asset.pricingModels.push(priceModel);
  }

  removePricingModel(index:number):void {
    this.asset.pricingModels.splice(index, 1);
  }

  submitForm():void {
    // TODO: submit form!
    this.uploading.status = true;
    this.uploading.errors = [];

    // fix dates format
    this.asset.metadataDate = moment(this.asset.metadataDate).format('YYYY-MM-DD');
    const config: AxiosRequestConfig = {
      onUploadProgress: (progressEvent: any): void => {
        const totalLength = progressEvent.lengthComputable ? progressEvent.total : progressEvent.target.getResponseHeader('content-length') || progressEvent.target.getResponseHeader('x-decompressed-content-length');
        if (totalLength !== null) {
          this.uploading.percentage = (Math.round((progressEvent.loaded * 100) / totalLength));
        }
      },
    };
    this.catalogueApi.create(this.asset, config)
      .then((response: ServerResponse<void>) => {
        if (response.success) {
          this.uploading.completed = true;
          this.uploading.title = 'Your asset has been submitted for review';
          this.uploading.subtitle = 'You’ll be notified by email for this process';
        } else {
          this.uploading.status = false;
          this.uploading.completed = true;
          this.uploading.errors = response.messages;
          setTimeout(() => {
            this.uploading.errors = [];
          }, 10000);
          setTimeout(() => {
            window.scrollTo(0, document.body.scrollHeight);
          }, 300);
        }
      })
      .catch((error: AxiosError) => {
        if (error.response) {
          this.uploading.errors = error.response.data.messages;
          setTimeout(() => {
            this.uploading.errors = [];
          }, 10000);
        }
        this.uploading.status = false;
      });
  }
}
</script>
<style lang="scss">
  @import "@/assets/styles/_forms.scss";
</style>
